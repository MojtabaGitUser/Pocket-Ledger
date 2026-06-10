package com.mojtaba.pocketledger.core.testing.repository

import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.data.repository.contract.SyncState
import com.mojtaba.pocketledger.core.data.search.SearchQuery
import com.mojtaba.pocketledger.core.data.search.SearchRecurringFilter
import com.mojtaba.pocketledger.core.data.search.SearchSort
import com.mojtaba.pocketledger.core.data.search.SearchTransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTransactionRepository(
    initialTransactions: List<LedgerTransaction> = emptyList(),
    initialTagLinks: List<TransactionTagLink> = emptyList(),
) : TransactionRepository {
    private val transactions = MutableStateFlow(initialTransactions.associateBy { it.id })
    private val transactionTagIds = MutableStateFlow(
        initialTagLinks
            .groupBy { it.transactionId }
            .mapValues { (_, links) -> links.map { it.tagId }.toSet() },
    )

    var deleteByIdCalls: Int = 0
        private set
    var upsertCalls: Int = 0
        private set
    var throwOnDeleteById: Boolean = false
    var throwOnUpsert: Boolean = false
    var throwOnSearch: Boolean = false
    var forcedDeleteByIdResult: Boolean? = null

    override val repositoryName: String = "fake-transactions"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(transaction: LedgerTransaction) = upsert(transaction)

    override suspend fun insertAll(transactions: List<LedgerTransaction>) = upsertAll(transactions)

    override suspend fun upsert(transaction: LedgerTransaction) {
        upsertCalls += 1
        if (throwOnUpsert) {
            error("Save failed")
        }
        transactions.update { it + (transaction.id to transaction) }
    }

    override suspend fun upsertAll(transactions: List<LedgerTransaction>) {
        this.transactions.update { current -> current + transactions.associateBy { it.id } }
    }

    override suspend fun update(transaction: LedgerTransaction) = upsert(transaction)

    override suspend fun delete(transaction: LedgerTransaction) {
        transactions.update { it - transaction.id }
    }

    override suspend fun deleteById(id: String): Boolean {
        deleteByIdCalls += 1
        if (throwOnDeleteById) {
            error("Delete failed")
        }
        forcedDeleteByIdResult?.let { return it }
        val existed = id in transactions.value
        transactions.update { it - id }
        return existed
    }

    override suspend fun getById(id: String): LedgerTransaction? = transactions.value[id]

    override fun observeById(id: String): Flow<LedgerTransaction?> = transactions.map { it[id] }

    override fun observeRecentTransactions(limit: Int): Flow<List<LedgerTransaction>> =
        transactions.map { transactionMap -> transactionMap.values.sortedForDisplay().take(limit) }

    override fun observeTransactionsByDateRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<LedgerTransaction>> =
        observeRecentTransactions(Int.MAX_VALUE).map { transactions ->
            transactions.filter { it.occurredAt in startInclusive..endInclusive }
        }

    override fun observeTransactionsByCategory(categoryId: String): Flow<List<LedgerTransaction>> =
        observeRecentTransactions(Int.MAX_VALUE).map { transactions ->
            transactions.filter { it.categoryId == categoryId }
        }

    override fun observeTransactionsByType(type: String): Flow<List<LedgerTransaction>> =
        observeRecentTransactions(Int.MAX_VALUE).map { transactions -> transactions.filter { it.type == type } }

    override fun observeTransactionsByTag(tagId: String): Flow<List<LedgerTransaction>> =
        transactions.map { transactionMap ->
            val matchingIds = transactionTagIds.value
                .filterValues { tagId in it }
                .keys
            transactionMap.values.filter { it.id in matchingIds }.sortedForDisplay()
        }

    override fun searchTransactions(query: SearchQuery): Flow<List<LedgerTransaction>> {
        if (throwOnSearch) {
            return flow { error("Search failed") }
        }

        val normalizedQuery = query.normalized()
        if (!normalizedQuery.validate().isValid) {
            return flowOf(emptyList())
        }

        return transactions.map { transactionMap ->
            transactionMap.values
                .filter { transaction -> transaction.matchesKeywordPrefix(normalizedQuery.text) }
                .filter { transaction -> transaction.matchesFilters(normalizedQuery) }
                .sortedForSearch(normalizedQuery.sort)
        }
    }

    fun containsTransaction(id: String): Boolean = id in transactions.value

    fun snapshot(): List<LedgerTransaction> = transactions.value.values.sortedForDisplay()

    fun setTagLinks(links: List<TransactionTagLink>) {
        transactionTagIds.value = links
            .groupBy { it.transactionId }
            .mapValues { (_, groupedLinks) -> groupedLinks.map { it.tagId }.toSet() }
    }

    private fun Iterable<LedgerTransaction>.sortedForDisplay(): List<LedgerTransaction> =
        sortedWith(
            compareByDescending<LedgerTransaction> { it.occurredAt }
                .thenByDescending { it.createdAt }
                .thenBy { it.id },
        )

    private fun LedgerTransaction.matchesKeywordPrefix(keyword: String): Boolean {
        if (keyword.isBlank()) {
            return true
        }

        return merchant.startsWithKeyword(keyword) ||
            note.startsWithKeyword(keyword) ||
            source.startsWithKeyword(keyword)
    }

    private fun String?.startsWithKeyword(keyword: String): Boolean =
        this?.startsWith(keyword, ignoreCase = true) == true

    private fun LedgerTransaction.matchesFilters(query: SearchQuery): Boolean {
        val filters = query.filters
        if (filters.transactionTypes.isNotEmpty() && type.normalizedType() !in filters.transactionTypes) {
            return false
        }
        if (filters.categoryIds.isNotEmpty() && categoryId !in filters.categoryIds) {
            return false
        }
        if (filters.tagIds.isNotEmpty()) {
            val linkedTagIds = transactionTagIds.value[id].orEmpty()
            if (!linkedTagIds.containsAll(filters.tagIds)) {
                return false
            }
        }
        filters.dateRange?.let { range ->
            val startMillis = range.startMillis
            val endMillis = range.endMillis
            if (startMillis != null && occurredAt < startMillis) {
                return false
            }
            if (endMillis != null && occurredAt > endMillis) {
                return false
            }
        }
        filters.amountRange?.let { range ->
            val absoluteAmount = kotlin.math.abs(amountMinor)
            val minMinor = range.minMinor
            val maxMinor = range.maxMinor
            if (minMinor != null && absoluteAmount < minMinor) {
                return false
            }
            if (maxMinor != null && absoluteAmount > maxMinor) {
                return false
            }
        }
        filters.currencyCode?.let { currency ->
            if (!currencyCode.equals(currency, ignoreCase = true)) {
                return false
            }
        }
        return when (filters.recurring) {
            SearchRecurringFilter.Any -> true
            SearchRecurringFilter.RecurringOnly -> isRecurring
            SearchRecurringFilter.NonRecurringOnly -> !isRecurring
        }
    }

    private fun String.normalizedType(): SearchTransactionType? =
        when {
            equals("income", ignoreCase = true) -> SearchTransactionType.Income
            equals("expense", ignoreCase = true) -> SearchTransactionType.Expense
            else -> null
        }

    private fun Iterable<LedgerTransaction>.sortedForSearch(sort: SearchSort): List<LedgerTransaction> =
        when (sort) {
            SearchSort.DateDescending -> sortedForDisplay()
            SearchSort.DateAscending -> sortedWith(
                compareBy<LedgerTransaction> { it.occurredAt }
                    .thenBy { it.createdAt }
                    .thenBy { it.id },
            )
            SearchSort.AmountDescending -> sortedWith(
                compareByDescending<LedgerTransaction> { it.amountMinor }
                    .thenByDescending { it.occurredAt }
                    .thenBy { it.id },
            )
            SearchSort.AmountAscending -> sortedWith(
                compareBy<LedgerTransaction> { it.amountMinor }
                    .thenByDescending { it.occurredAt }
                    .thenBy { it.id },
            )
            SearchSort.CategoryAscending -> sortedWith(
                compareBy<LedgerTransaction> { it.categoryId }
                    .thenByDescending { it.occurredAt }
                    .thenBy { it.id },
            )
            SearchSort.RecentlyUpdated -> sortedWith(
                compareByDescending<LedgerTransaction> { it.updatedAt }
                    .thenBy { it.id },
            )
        }
}
