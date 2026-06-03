package com.mojtaba.pocketledger.feature.transaction.testing

import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.data.repository.contract.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestTransactionRepository(
    initialTransactions: List<LedgerTransaction> = emptyList(),
) : TransactionRepository {
    private val transactions = MutableStateFlow(initialTransactions.associateBy { it.id })
    var deleteByIdCalls = 0
    var upsertCalls = 0
    var throwOnDeleteById = false
    var forceDeleteByIdResult: Boolean? = null

    override val repositoryName: String = "test-transactions"
    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(transaction: LedgerTransaction) = upsert(transaction)
    override suspend fun insertAll(transactions: List<LedgerTransaction>) = upsertAll(transactions)
    override suspend fun upsert(transaction: LedgerTransaction) {
        upsertCalls += 1
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
        forceDeleteByIdResult?.let { return it }
        val existed = transactions.value.containsKey(id)
        transactions.update { it - id }
        return existed
    }
    override suspend fun getById(id: String): LedgerTransaction? = transactions.value[id]
    override fun observeById(id: String): Flow<LedgerTransaction?> = transactions.map { it[id] }
    override fun observeRecentTransactions(limit: Int): Flow<List<LedgerTransaction>> =
        transactions.map { items ->
            items.values.sortedWith(
                compareByDescending<LedgerTransaction> { it.occurredAt }
                    .thenByDescending { it.createdAt }
                    .thenBy { it.id },
            ).take(limit)
        }
    override fun observeTransactionsByDateRange(startInclusive: Long, endInclusive: Long): Flow<List<LedgerTransaction>> =
        observeRecentTransactions(Int.MAX_VALUE).map { items ->
            items.filter { it.occurredAt in startInclusive..endInclusive }
        }
    override fun observeTransactionsByCategory(categoryId: String): Flow<List<LedgerTransaction>> =
        observeRecentTransactions(Int.MAX_VALUE).map { items -> items.filter { it.categoryId == categoryId } }
    override fun observeTransactionsByType(type: String): Flow<List<LedgerTransaction>> =
        observeRecentTransactions(Int.MAX_VALUE).map { items -> items.filter { it.type == type } }
    override fun observeTransactionsByTag(tagId: String): Flow<List<LedgerTransaction>> = flowOf(emptyList())

    fun containsTransaction(id: String): Boolean = transactions.value.containsKey(id)
}

class TestCategoryRepository(
    initialCategories: List<LedgerCategory> = listOf(testCategory("food", "Food", "expense")),
) : CategoryRepository {
    private val categories = MutableStateFlow(initialCategories.associateBy { it.id })

    override val repositoryName: String = "test-categories"
    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())
    override suspend fun insert(category: LedgerCategory) = upsert(category)
    override suspend fun insertAll(categories: List<LedgerCategory>) = upsertAll(categories)
    override suspend fun upsert(category: LedgerCategory) {
        categories.update { it + (category.id to category) }
    }
    override suspend fun upsertAll(categories: List<LedgerCategory>) {
        this.categories.update { it + categories.associateBy { category -> category.id } }
    }
    override suspend fun update(category: LedgerCategory) = upsert(category)
    override suspend fun delete(category: LedgerCategory) {
        categories.update { it - category.id }
    }
    override suspend fun deleteById(id: String): Boolean {
        val existed = categories.value.containsKey(id)
        categories.update { it - id }
        return existed
    }
    override suspend fun getById(id: String): LedgerCategory? = categories.value[id]
    override fun observeById(id: String): Flow<LedgerCategory?> = categories.map { it[id] }
    override fun observeAll(): Flow<List<LedgerCategory>> = categories.map { it.values.toList() }
    override fun observeActiveCategories(): Flow<List<LedgerCategory>> =
        categories.map { items -> items.values.filter { it.isActive } }
    override fun observeActiveCategoriesByType(type: String): Flow<List<LedgerCategory>> =
        observeActiveCategories().map { items -> items.filter { it.type == type } }
}

class TestTagRepository(
    initialTags: List<LedgerTag> = listOf(testTag("work", "Work")),
    initialLinks: Map<String, Set<String>> = emptyMap(),
) : TagRepository {
    private val tags = MutableStateFlow(initialTags.associateBy { it.id })
    private val links = MutableStateFlow(initialLinks)

    override val repositoryName: String = "test-tags"
    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())
    override suspend fun insert(tag: LedgerTag) = upsert(tag)
    override suspend fun insertAll(tags: List<LedgerTag>) = upsertAll(tags)
    override suspend fun upsert(tag: LedgerTag) {
        tags.update { it + (tag.id to tag) }
    }
    override suspend fun upsertAll(tags: List<LedgerTag>) {
        this.tags.update { it + tags.associateBy { tag -> tag.id } }
    }
    override suspend fun delete(tag: LedgerTag) {
        tags.update { it - tag.id }
    }
    override suspend fun deleteById(id: String): Boolean {
        val existed = tags.value.containsKey(id)
        tags.update { it - id }
        return existed
    }
    override suspend fun getById(id: String): LedgerTag? = tags.value[id]
    override fun observeById(id: String): Flow<LedgerTag?> = tags.map { it[id] }
    override fun observeTags(): Flow<List<LedgerTag>> = tags.map { it.values.toList() }
    override suspend fun addTagToTransaction(link: TransactionTagLink) {
        links.update { current ->
            current + (link.transactionId to (current[link.transactionId].orEmpty() + link.tagId))
        }
    }
    override suspend fun removeTagFromTransaction(transactionId: String, tagId: String): Boolean {
        val existing = links.value[transactionId].orEmpty()
        links.update { it + (transactionId to (existing - tagId)) }
        return tagId in existing
    }
    override fun observeTagsForTransaction(transactionId: String): Flow<List<LedgerTag>> =
        tags.combineWith(links) { tagMap, linkMap ->
            linkMap[transactionId].orEmpty().mapNotNull(tagMap::get)
        }

    fun tagIdsForTransaction(transactionId: String): Set<String> =
        links.value[transactionId].orEmpty()

    private fun <A, B, R> Flow<A>.combineWith(other: Flow<B>, transform: (A, B) -> R): Flow<R> =
        kotlinx.coroutines.flow.combine(this, other) { a, b -> transform(a, b) }
}

fun testTransaction(
    id: String = "transaction-1",
    amountMinor: Long = -4_250,
    type: String = "expense",
    categoryId: String? = "food",
    merchant: String? = "Coffee Shop",
    note: String? = "Team breakfast",
    occurredAt: Long = 1_700_000_000_000L,
): LedgerTransaction = LedgerTransaction(
    id = id,
    amountMinor = amountMinor,
    currencyCode = "USD",
    type = type,
    occurredAt = occurredAt,
    categoryId = categoryId,
    merchant = merchant,
    note = note,
    source = "manual",
    isRecurring = false,
    createdAt = 1_700_000_000_000L,
    updatedAt = 1_700_000_900_000L,
)

fun testCategory(
    id: String,
    name: String,
    type: String,
): LedgerCategory = LedgerCategory(
    id = id,
    name = name,
    type = type,
    createdAt = 1L,
    updatedAt = 1L,
)

fun testTag(
    id: String,
    name: String,
): LedgerTag = LedgerTag(
    id = id,
    name = name,
    createdAt = 1L,
    updatedAt = 1L,
)
