package com.mojtaba.folentra.feature.transaction.presentation.list

import com.mojtaba.folentra.core.testing.coroutine.MainDispatcherRule
import com.mojtaba.folentra.core.testing.fixture.TestClock
import com.mojtaba.folentra.core.testing.fixture.testLedgerCategory
import com.mojtaba.folentra.core.testing.fixture.testLedgerTag
import com.mojtaba.folentra.core.testing.fixture.testLedgerTransaction
import com.mojtaba.folentra.core.testing.fixture.testTransactionTagLink
import com.mojtaba.folentra.core.testing.repository.FakeCategoryRepository
import com.mojtaba.folentra.core.testing.repository.FakeTagRepository
import com.mojtaba.folentra.core.testing.repository.FakeTransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun emitsEmptyStateWhenRepositoryHasNoTransactions() = runTest {
        val viewModel = newViewModel(transactionRepository = FakeTransactionRepository())
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertEquals(TransactionListUiState.Empty, viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun emitsContentStateWhenRepositoryHasTransactions() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(listOf(testTransaction())),
        )
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TransactionListUiState.Content)
        assertEquals(1, (state as TransactionListUiState.Content).transactions.size)
        job.cancel()
    }

    @Test
    fun rowModelIncludesAmountCategoryDateNoteAndTags() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(
                listOf(testTransaction(amountMinor = -4_250, categoryId = "food", note = "Team breakfast")),
            ),
            categoryRepository = FakeCategoryRepository(listOf(testLedgerCategory(id = "food", name = "Food", type = "expense"))),
            tagRepository = FakeTagRepository(
                initialTags = listOf(testLedgerTag(id = "work", name = "Work")),
                initialLinks = listOf(testTransactionTagLink("transaction-1", "work")),
            ),
        )
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val item = (viewModel.uiState.value as TransactionListUiState.Content).transactions.single()
        assertEquals("-\$42.50", item.amount.text)
        assertEquals("Food", item.categoryLabel)
        assertEquals("Nov 14, 2023", item.dateLabel)
        assertEquals("Team breakfast", item.notePreview)
        assertEquals(listOf("Work"), item.tagLabels)
        job.cancel()
    }

    @Test
    fun rowModelUsesFallbackCategoryWhenCategoryIsMissing() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(listOf(testTransaction(categoryId = "missing"))),
            categoryRepository = FakeCategoryRepository(emptyList()),
        )
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val item = (viewModel.uiState.value as TransactionListUiState.Content).transactions.single()
        assertEquals("Uncategorized", item.categoryLabel)
        job.cancel()
    }

    @Test
    fun emitsUpdatesWhenRepositoryChanges() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val viewModel = newViewModel(transactionRepository = transactionRepository)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(TransactionListUiState.Empty, viewModel.uiState.value)

        transactionRepository.upsert(testTransaction())
        advanceUntilIdle()

        val state = viewModel.uiState.value as TransactionListUiState.Content
        assertEquals(listOf("transaction-1"), state.transactions.map { it.id })
        job.cancel()
    }

    @Test
    fun transactionClickEmitsOpenDetailEvent() = runTest {
        val viewModel = newViewModel()
        val events = mutableListOf<TransactionListEvent>()
        val job = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(TransactionListAction.TransactionClicked("transaction-1"))
        advanceUntilIdle()

        assertEquals(TransactionListEvent.OpenDetail("transaction-1"), events.single())
        job.cancel()
    }

    private fun newViewModel(
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(
            listOf(testLedgerCategory(id = "food", name = "Food", type = "expense")),
        ),
        tagRepository: FakeTagRepository = FakeTagRepository(),
    ): TransactionListViewModel = TransactionListViewModel(
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        tagRepository = tagRepository,
    )

    private fun testTransaction(
        id: String = "transaction-1",
        amountMinor: Long = -4_250,
        categoryId: String? = "food",
        note: String? = "Team breakfast",
    ) = testLedgerTransaction(
        id = id,
        amountMinor = amountMinor,
        categoryId = categoryId,
        merchant = "Coffee Shop",
        note = note,
        occurredAt = TestClock.November14,
    )
}
