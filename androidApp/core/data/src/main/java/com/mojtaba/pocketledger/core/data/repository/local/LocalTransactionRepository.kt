package com.mojtaba.pocketledger.core.data.repository.local

import com.mojtaba.pocketledger.core.data.mapper.asEntity
import com.mojtaba.pocketledger.core.data.mapper.asExternalModel
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.data.repository.contract.SyncState
import com.mojtaba.pocketledger.core.data.search.SearchQuery
import com.mojtaba.pocketledger.core.data.search.SearchSort
import com.mojtaba.pocketledger.core.data.search.SearchTransactionType
import com.mojtaba.pocketledger.core.database.dao.TransactionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LocalTransactionRepository(
    private val transactionDao: TransactionDao,
) : TransactionRepository {
    override val repositoryName: String = "transactions"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(transaction: LedgerTransaction) {
        transactionDao.insert(transaction.asEntity())
    }

    override suspend fun insertAll(transactions: List<LedgerTransaction>) {
        transactionDao.insertAll(transactions.map { it.asEntity() })
    }

    override suspend fun upsert(transaction: LedgerTransaction) {
        transactionDao.upsert(transaction.asEntity())
    }

    override suspend fun upsertAll(transactions: List<LedgerTransaction>) {
        transactionDao.upsertAll(transactions.map { it.asEntity() })
    }

    override suspend fun update(transaction: LedgerTransaction) {
        transactionDao.update(transaction.asEntity())
    }

    override suspend fun delete(transaction: LedgerTransaction) {
        transactionDao.delete(transaction.asEntity())
    }

    override suspend fun deleteById(id: String): Boolean = transactionDao.deleteById(id) > 0

    override suspend fun getById(id: String): LedgerTransaction? =
        transactionDao.getById(id)?.asExternalModel()

    override fun observeById(id: String): Flow<LedgerTransaction?> =
        transactionDao.observeById(id).map { it?.asExternalModel() }

    override fun observeRecentTransactions(limit: Int): Flow<List<LedgerTransaction>> =
        transactionDao.observeRecentTransactions(limit).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeTransactionsByDateRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<LedgerTransaction>> =
        transactionDao.observeTransactionsByDateRange(startInclusive, endInclusive).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeTransactionsByCategory(categoryId: String): Flow<List<LedgerTransaction>> =
        transactionDao.observeTransactionsByCategory(categoryId).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeTransactionsByType(type: String): Flow<List<LedgerTransaction>> =
        transactionDao.observeTransactionsByType(type).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeTransactionsByTag(tagId: String): Flow<List<LedgerTransaction>> =
        transactionDao.observeTransactionsByTag(tagId).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun searchTransactions(query: SearchQuery): Flow<List<LedgerTransaction>> {
        val normalizedQuery = query.normalized()
        if (!normalizedQuery.validate().isValid) {
            return flowOf(emptyList())
        }

        val parameters = normalizedQuery.toSearchParameters()
        return transactionDao.searchTransactions(parameters, normalizedQuery.sort).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }
}

private fun TransactionDao.searchTransactions(
    parameters: SearchParameters,
    sort: SearchSort,
): Flow<List<com.mojtaba.pocketledger.core.database.model.TransactionEntity>> =
    when (sort) {
        SearchSort.DateDescending -> searchTransactionsByDateDescending(
            keywordPattern = parameters.keywordPattern,
            types = parameters.types,
            typeCount = parameters.typeCount,
            categoryIds = parameters.categoryIds,
            categoryCount = parameters.categoryCount,
            tagIds = parameters.tagIds,
            tagCount = parameters.tagCount,
            startMillis = parameters.startMillis,
            endMillis = parameters.endMillis,
            minAmountMinor = parameters.minAmountMinor,
            maxAmountMinor = parameters.maxAmountMinor,
        )
        SearchSort.DateAscending -> searchTransactionsByDateAscending(
            keywordPattern = parameters.keywordPattern,
            types = parameters.types,
            typeCount = parameters.typeCount,
            categoryIds = parameters.categoryIds,
            categoryCount = parameters.categoryCount,
            tagIds = parameters.tagIds,
            tagCount = parameters.tagCount,
            startMillis = parameters.startMillis,
            endMillis = parameters.endMillis,
            minAmountMinor = parameters.minAmountMinor,
            maxAmountMinor = parameters.maxAmountMinor,
        )
        SearchSort.AmountDescending -> searchTransactionsByAmountDescending(
            keywordPattern = parameters.keywordPattern,
            types = parameters.types,
            typeCount = parameters.typeCount,
            categoryIds = parameters.categoryIds,
            categoryCount = parameters.categoryCount,
            tagIds = parameters.tagIds,
            tagCount = parameters.tagCount,
            startMillis = parameters.startMillis,
            endMillis = parameters.endMillis,
            minAmountMinor = parameters.minAmountMinor,
            maxAmountMinor = parameters.maxAmountMinor,
        )
        SearchSort.AmountAscending -> searchTransactionsByAmountAscending(
            keywordPattern = parameters.keywordPattern,
            types = parameters.types,
            typeCount = parameters.typeCount,
            categoryIds = parameters.categoryIds,
            categoryCount = parameters.categoryCount,
            tagIds = parameters.tagIds,
            tagCount = parameters.tagCount,
            startMillis = parameters.startMillis,
            endMillis = parameters.endMillis,
            minAmountMinor = parameters.minAmountMinor,
            maxAmountMinor = parameters.maxAmountMinor,
        )
        SearchSort.CategoryAscending -> searchTransactionsByCategoryAscending(
            keywordPattern = parameters.keywordPattern,
            types = parameters.types,
            typeCount = parameters.typeCount,
            categoryIds = parameters.categoryIds,
            categoryCount = parameters.categoryCount,
            tagIds = parameters.tagIds,
            tagCount = parameters.tagCount,
            startMillis = parameters.startMillis,
            endMillis = parameters.endMillis,
            minAmountMinor = parameters.minAmountMinor,
            maxAmountMinor = parameters.maxAmountMinor,
        )
        SearchSort.RecentlyUpdated -> searchTransactionsByRecentlyUpdated(
            keywordPattern = parameters.keywordPattern,
            types = parameters.types,
            typeCount = parameters.typeCount,
            categoryIds = parameters.categoryIds,
            categoryCount = parameters.categoryCount,
            tagIds = parameters.tagIds,
            tagCount = parameters.tagCount,
            startMillis = parameters.startMillis,
            endMillis = parameters.endMillis,
            minAmountMinor = parameters.minAmountMinor,
            maxAmountMinor = parameters.maxAmountMinor,
        )
    }

private data class SearchParameters(
    val keywordPattern: String?,
    val types: Set<String>,
    val typeCount: Int,
    val categoryIds: Set<String>,
    val categoryCount: Int,
    val tagIds: Set<String>,
    val tagCount: Int,
    val startMillis: Long?,
    val endMillis: Long?,
    val minAmountMinor: Long?,
    val maxAmountMinor: Long?,
)

private fun SearchQuery.toSearchParameters(): SearchParameters {
    val typeValues = filters.transactionTypes.map { it.databaseValue() }.toSet()
    return SearchParameters(
        keywordPattern = text
            .takeIf { it.isNotBlank() }
            ?.escapeSqlLike()
            ?.plus("%"),
        types = typeValues.orPlaceholder(),
        typeCount = typeValues.size,
        categoryIds = filters.categoryIds.orPlaceholder(),
        categoryCount = filters.categoryIds.size,
        tagIds = filters.tagIds.orPlaceholder(),
        tagCount = filters.tagIds.size,
        startMillis = filters.dateRange?.startMillis,
        endMillis = filters.dateRange?.endMillis,
        minAmountMinor = filters.amountRange?.minMinor,
        maxAmountMinor = filters.amountRange?.maxMinor,
    )
}

private fun SearchTransactionType.databaseValue(): String =
    when (this) {
        SearchTransactionType.Income -> "income"
        SearchTransactionType.Expense -> "expense"
    }

private fun Set<String>.orPlaceholder(): Set<String> =
    if (isEmpty()) setOf(NO_FILTER_PLACEHOLDER) else this

private fun String.escapeSqlLike(): String =
    buildString(length) {
        this@escapeSqlLike.forEach { char ->
            when (char) {
                '\\', '%', '_' -> append('\\')
            }
            append(char)
        }
    }

private const val NO_FILTER_PLACEHOLDER = "__pocket_ledger_no_filter__"
