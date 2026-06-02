package com.mojtaba.pocketledger.feature.transaction.presentation.editor

import androidx.lifecycle.SavedStateHandle
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.data.repository.contract.SyncState
import com.mojtaba.pocketledger.feature.transaction.form.AmountError
import com.mojtaba.pocketledger.feature.transaction.form.TransactionFormMode
import com.mojtaba.pocketledger.feature.transaction.form.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun updatesFormStateAndValidation() = runTest {
        val viewModel = newViewModel()

        viewModel.onAction(TransactionEditorAction.AmountChanged("abc"))
        advanceUntilIdle()

        assertEquals("abc", viewModel.uiState.value.formState.amountInput)
        assertEquals(AmountError.INVALID_FORMAT, viewModel.uiState.value.validationResult.errors.amount)

        viewModel.onAction(TransactionEditorAction.AmountChanged("12.34"))
        viewModel.onAction(TransactionEditorAction.CategoryChanged("food"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.validationResult.isValid)
    }

    @Test
    fun saveValidCreateWritesTransactionAndTags() = runTest {
        val transactionRepository = TestTransactionRepository()
        val tagRepository = TestTagRepository()
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            tagRepository = tagRepository,
            idGenerator = { "generated-id" },
        )
        viewModel.onAction(TransactionEditorAction.AmountChanged("10.50"))
        viewModel.onAction(TransactionEditorAction.CategoryChanged("food"))
        viewModel.onAction(TransactionEditorAction.TagToggled("work"))
        viewModel.onAction(TransactionEditorAction.SaveClicked)
        advanceUntilIdle()

        val saved = transactionRepository.transactions["generated-id"]
        assertNotNull(saved)
        assertEquals(-1_050L, saved?.amountMinor)
        assertEquals("expense", saved?.type)
        assertEquals("food", saved?.categoryId)
        assertEquals(setOf("work"), tagRepository.links["generated-id"])
    }

    @Test
    fun saveInvalidFormDoesNotWriteTransaction() = runTest {
        val transactionRepository = TestTransactionRepository()
        val viewModel = newViewModel(transactionRepository = transactionRepository)

        viewModel.onAction(TransactionEditorAction.AmountChanged(""))
        viewModel.onAction(TransactionEditorAction.SaveClicked)
        advanceUntilIdle()

        assertTrue(transactionRepository.transactions.isEmpty())
        assertFalse(viewModel.uiState.value.validationResult.isValid)
    }

    @Test
    fun editModeLoadsExistingTransactionAndTags() = runTest {
        val transactionRepository = TestTransactionRepository(
            initialTransactions = mutableMapOf(
                "transaction-1" to testTransaction(
                    id = "transaction-1",
                    amountMinor = -1_250,
                    categoryId = "food",
                    merchant = "Cafe",
                    note = "Lunch",
                ),
            ),
        )
        val tagRepository = TestTagRepository(
            initialLinks = mutableMapOf("transaction-1" to mutableSetOf("work")),
        )
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            tagRepository = tagRepository,
            mode = TransactionFormMode.EDIT,
            transactionId = "transaction-1",
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(TransactionFormMode.EDIT, state.formState.mode)
        assertEquals("transaction-1", state.formState.transactionId)
        assertEquals("12.5", state.formState.amountInput)
        assertEquals(TransactionType.EXPENSE, state.formState.transactionType)
        assertEquals("food", state.formState.categoryId)
        assertEquals("Cafe", state.formState.merchant)
        assertEquals("Lunch", state.formState.note)
        assertEquals(setOf("work"), state.selectedTagIds)
    }

    private fun newViewModel(
        transactionRepository: TestTransactionRepository = TestTransactionRepository(),
        categoryRepository: TestCategoryRepository = TestCategoryRepository(),
        tagRepository: TestTagRepository = TestTagRepository(),
        mode: TransactionFormMode = TransactionFormMode.CREATE,
        transactionId: String? = null,
        idGenerator: () -> String = { "generated-id" },
    ): TransactionEditorViewModel = TransactionEditorViewModel(
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        tagRepository = tagRepository,
        savedStateHandle = SavedStateHandle(),
        initialMode = mode,
        initialTransactionId = transactionId,
        currentTimeMillis = { CURRENT_TIME },
        idGenerator = idGenerator,
    )

    private companion object {
        const val CURRENT_TIME = 1_700_000_000_000L
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class TestTransactionRepository(
    val transactions: MutableMap<String, LedgerTransaction> = mutableMapOf(),
    initialTransactions: MutableMap<String, LedgerTransaction> = mutableMapOf(),
) : TransactionRepository {
    init {
        transactions.putAll(initialTransactions)
    }

    override val repositoryName: String = "test-transactions"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(transaction: LedgerTransaction) {
        transactions[transaction.id] = transaction
    }

    override suspend fun insertAll(transactions: List<LedgerTransaction>) {
        transactions.forEach { insert(it) }
    }

    override suspend fun upsert(transaction: LedgerTransaction) {
        transactions[transaction.id] = transaction
    }

    override suspend fun upsertAll(transactions: List<LedgerTransaction>) {
        transactions.forEach { upsert(it) }
    }

    override suspend fun update(transaction: LedgerTransaction) {
        transactions[transaction.id] = transaction
    }

    override suspend fun delete(transaction: LedgerTransaction) {
        transactions.remove(transaction.id)
    }

    override suspend fun deleteById(id: String): Boolean = transactions.remove(id) != null

    override suspend fun getById(id: String): LedgerTransaction? = transactions[id]

    override fun observeById(id: String): Flow<LedgerTransaction?> =
        flowOf(transactions[id])

    override fun observeRecentTransactions(limit: Int): Flow<List<LedgerTransaction>> =
        flowOf(transactions.values.take(limit))

    override fun observeTransactionsByDateRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<LedgerTransaction>> = flowOf(
        transactions.values.filter { it.occurredAt in startInclusive..endInclusive },
    )

    override fun observeTransactionsByCategory(categoryId: String): Flow<List<LedgerTransaction>> =
        flowOf(transactions.values.filter { it.categoryId == categoryId })

    override fun observeTransactionsByType(type: String): Flow<List<LedgerTransaction>> =
        flowOf(transactions.values.filter { it.type == type })

    override fun observeTransactionsByTag(tagId: String): Flow<List<LedgerTransaction>> = flowOf(emptyList())
}

private class TestCategoryRepository : CategoryRepository {
    private val categories = MutableStateFlow(
        listOf(
            testCategory("food", "Food", "expense"),
            testCategory("salary", "Salary", "income"),
        ),
    )

    override val repositoryName: String = "test-categories"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(category: LedgerCategory) = Unit
    override suspend fun insertAll(categories: List<LedgerCategory>) = Unit
    override suspend fun upsert(category: LedgerCategory) = Unit
    override suspend fun upsertAll(categories: List<LedgerCategory>) = Unit
    override suspend fun update(category: LedgerCategory) = Unit
    override suspend fun delete(category: LedgerCategory) = Unit
    override suspend fun deleteById(id: String): Boolean = false
    override suspend fun getById(id: String): LedgerCategory? = categories.value.firstOrNull { it.id == id }
    override fun observeById(id: String): Flow<LedgerCategory?> = categories.map { items -> items.firstOrNull { it.id == id } }
    override fun observeAll(): Flow<List<LedgerCategory>> = categories
    override fun observeActiveCategories(): Flow<List<LedgerCategory>> = categories
    override fun observeActiveCategoriesByType(type: String): Flow<List<LedgerCategory>> =
        categories.map { items -> items.filter { it.type == type } }
}

private class TestTagRepository(
    initialLinks: MutableMap<String, MutableSet<String>> = mutableMapOf(),
) : TagRepository {
    val links: MutableMap<String, MutableSet<String>> = initialLinks
    private val tags = MutableStateFlow(
        listOf(
            testTag("work", "Work"),
            testTag("weekend", "Weekend"),
        ),
    )

    override val repositoryName: String = "test-tags"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(tag: LedgerTag) = Unit
    override suspend fun insertAll(tags: List<LedgerTag>) = Unit
    override suspend fun upsert(tag: LedgerTag) = Unit
    override suspend fun upsertAll(tags: List<LedgerTag>) = Unit
    override suspend fun delete(tag: LedgerTag) = Unit
    override suspend fun deleteById(id: String): Boolean = false
    override suspend fun getById(id: String): LedgerTag? = tags.value.firstOrNull { it.id == id }
    override fun observeById(id: String): Flow<LedgerTag?> = tags.map { items -> items.firstOrNull { it.id == id } }
    override fun observeTags(): Flow<List<LedgerTag>> = tags

    override suspend fun addTagToTransaction(link: TransactionTagLink) {
        links.getOrPut(link.transactionId) { mutableSetOf() }.add(link.tagId)
    }

    override suspend fun removeTagFromTransaction(transactionId: String, tagId: String): Boolean =
        links[transactionId]?.remove(tagId) ?: false

    override fun observeTagsForTransaction(transactionId: String): Flow<List<LedgerTag>> =
        tags.map { items -> items.filter { it.id in (links[transactionId] ?: emptySet()) } }
}

private fun testCategory(
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

private fun testTag(
    id: String,
    name: String,
): LedgerTag = LedgerTag(
    id = id,
    name = name,
    createdAt = 1L,
    updatedAt = 1L,
)

private fun testTransaction(
    id: String,
    amountMinor: Long,
    categoryId: String?,
    merchant: String,
    note: String,
): LedgerTransaction = LedgerTransaction(
    id = id,
    amountMinor = amountMinor,
    currencyCode = "USD",
    type = if (amountMinor >= 0) "income" else "expense",
    occurredAt = 1_699_999_999_000L,
    categoryId = categoryId,
    merchant = merchant,
    note = note,
    source = "manual",
    isRecurring = false,
    createdAt = 1L,
    updatedAt = 1L,
)
