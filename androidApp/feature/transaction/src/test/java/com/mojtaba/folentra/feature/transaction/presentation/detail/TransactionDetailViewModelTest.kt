package com.mojtaba.folentra.feature.transaction.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.mojtaba.folentra.core.testing.coroutine.MainDispatcherRule
import com.mojtaba.folentra.core.testing.fixture.TestClock
import com.mojtaba.folentra.core.testing.fixture.testLedgerCategory
import com.mojtaba.folentra.core.testing.fixture.testLedgerTag
import com.mojtaba.folentra.core.testing.fixture.testLedgerTransaction
import com.mojtaba.folentra.core.testing.fixture.testTransactionTagLink
import com.mojtaba.folentra.core.testing.repository.FakeCategoryRepository
import com.mojtaba.folentra.core.testing.repository.FakeTagRepository
import com.mojtaba.folentra.core.testing.repository.FakeTransactionRepository
import com.mojtaba.folentra.feature.transaction.navigation.TransactionRoutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun emitsContentForValidId() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(listOf(testTransaction())),
            tagRepository = FakeTagRepository(
                initialTags = listOf(testLedgerTag(id = "work", name = "Work")),
                initialLinks = listOf(testTransactionTagLink("transaction-1", "work")),
            ),
            transactionId = "transaction-1",
        )
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TransactionDetailUiState.Content)
        val transaction = (state as TransactionDetailUiState.Content).transaction
        assertEquals("-\$42.50", transaction.amount.text)
        assertEquals("Expense", transaction.typeLabel)
        assertEquals("Food", transaction.categoryLabel)
        assertEquals("Nov 14, 2023", transaction.dateLabel)
        assertEquals("Coffee Shop", transaction.merchantLabel)
        assertEquals("Team breakfast", transaction.noteLabel)
        assertEquals(listOf("Work"), transaction.tagLabels)
        assertNotNull(transaction.createdAtLabel)
        assertNotNull(transaction.updatedAtLabel)
        assertTrue(transaction.createdAtLabel.startsWith("Nov 14, 2023"))
        assertTrue(transaction.updatedAtLabel.startsWith("Nov 14, 2023"))
        job.cancel()
    }

    @Test
    fun emitsNotFoundForMissingId() = runTest {
        val viewModel = newViewModel(transactionId = "missing")
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertEquals(TransactionDetailUiState.NotFound, viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun editActionEmitsExpectedEvent() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(listOf(testTransaction())),
            transactionId = "transaction-1",
        )
        val events = mutableListOf<TransactionDetailEvent>()
        val job = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(TransactionDetailAction.EditClicked)
        advanceUntilIdle()

        assertEquals(TransactionDetailEvent.EditTransaction("transaction-1"), events.single())
        job.cancel()
    }

    @Test
    fun deleteClickShowsConfirmation() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(testTransaction()))
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            transactionId = "transaction-1",
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailAction.DeleteClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value as TransactionDetailUiState.Content
        assertTrue(state.showDeleteConfirmation)
        assertEquals(0, transactionRepository.deleteByIdCalls)
        job.cancel()
    }

    @Test
    fun cancelDeleteHidesConfirmationAndDoesNotDelete() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(testTransaction()))
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            transactionId = "transaction-1",
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailAction.DeleteClicked)
        viewModel.onAction(TransactionDetailAction.DeleteDismissed)
        advanceUntilIdle()

        val state = viewModel.uiState.value as TransactionDetailUiState.Content
        assertFalse(state.showDeleteConfirmation)
        assertEquals(0, transactionRepository.deleteByIdCalls)
        job.cancel()
    }

    @Test
    fun confirmDeleteDeletesTransactionAndEmitsUndoSnackbar() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(testTransaction()))
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            transactionId = "transaction-1",
        )
        val events = mutableListOf<TransactionDetailEvent>()
        val uiJob = launch { viewModel.uiState.collect {} }
        val eventJob = launch { viewModel.events.collect(events::add) }
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailAction.DeleteClicked)
        viewModel.onAction(TransactionDetailAction.DeleteConfirmed)
        advanceUntilIdle()

        assertEquals(1, transactionRepository.deleteByIdCalls)
        assertFalse(transactionRepository.containsTransaction("transaction-1"))
        assertTrue(TransactionDetailEvent.ShowDeleteUndoSnackbar in events)
        assertEquals(TransactionDetailUiState.NotFound, viewModel.uiState.value)
        uiJob.cancel()
        eventJob.cancel()
    }

    @Test
    fun undoRestoresDeletedTransactionAndTags() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(testTransaction()))
        val tagRepository = FakeTagRepository(
            initialTags = listOf(testLedgerTag(id = "work", name = "Work")),
            initialLinks = listOf(testTransactionTagLink("transaction-1", "work")),
        )
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            tagRepository = tagRepository,
            transactionId = "transaction-1",
        )
        val uiJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailAction.DeleteClicked)
        viewModel.onAction(TransactionDetailAction.DeleteConfirmed)
        advanceUntilIdle()
        viewModel.onAction(TransactionDetailAction.UndoDeleteClicked)
        advanceUntilIdle()

        assertTrue(transactionRepository.containsTransaction("transaction-1"))
        assertEquals(1, transactionRepository.upsertCalls)
        assertEquals(setOf("work"), tagRepository.tagIdsForTransaction("transaction-1"))
        uiJob.cancel()
    }

    @Test
    fun undoWithoutSnapshotEmitsSafeError() = runTest {
        val viewModel = newViewModel(transactionId = "transaction-1")
        val events = mutableListOf<TransactionDetailEvent>()
        val job = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(TransactionDetailAction.UndoDeleteClicked)
        advanceUntilIdle()

        assertEquals(
            TransactionDetailEvent.ShowDeleteFailedSnackbar("Unable to restore transaction."),
            events.single(),
        )
        job.cancel()
    }

    @Test
    fun missingTransactionDeleteEmitsSafeError() = runTest {
        val viewModel = newViewModel(transactionId = "missing")
        val events = mutableListOf<TransactionDetailEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(TransactionDetailAction.DeleteConfirmed)
        advanceUntilIdle()

        assertEquals(
            TransactionDetailEvent.ShowDeleteFailedSnackbar("Transaction not found."),
            events.single(),
        )
        eventJob.cancel()
    }

    @Test
    fun doubleConfirmDoesNotDeleteTwice() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(testTransaction()))
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            transactionId = "transaction-1",
        )
        val uiJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailAction.DeleteClicked)
        viewModel.onAction(TransactionDetailAction.DeleteConfirmed)
        viewModel.onAction(TransactionDetailAction.DeleteConfirmed)
        advanceUntilIdle()

        assertEquals(1, transactionRepository.deleteByIdCalls)
        uiJob.cancel()
    }

    @Test
    fun deleteFailureEmitsErrorEvent() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(testTransaction())).apply {
            throwOnDeleteById = true
        }
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            transactionId = "transaction-1",
        )
        val events = mutableListOf<TransactionDetailEvent>()
        val uiJob = launch { viewModel.uiState.collect {} }
        val eventJob = launch { viewModel.events.collect(events::add) }
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailAction.DeleteClicked)
        viewModel.onAction(TransactionDetailAction.DeleteConfirmed)
        advanceUntilIdle()

        assertEquals(1, transactionRepository.deleteByIdCalls)
        assertTrue(events.contains(TransactionDetailEvent.ShowDeleteFailedSnackbar("Delete failed")))
        assertTrue(transactionRepository.containsTransaction("transaction-1"))
        uiJob.cancel()
        eventJob.cancel()
    }

    private fun newViewModel(
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(
            listOf(testLedgerCategory(id = "food", name = "Food", type = "expense")),
        ),
        tagRepository: FakeTagRepository = FakeTagRepository(),
        transactionId: String,
    ): TransactionDetailViewModel = TransactionDetailViewModel(
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        tagRepository = tagRepository,
        savedStateHandle = SavedStateHandle(
            mapOf(TransactionRoutes.TransactionIdArg to transactionId),
        ),
    )

    private fun testTransaction() = testLedgerTransaction(
        id = "transaction-1",
        amountMinor = -4_250,
        categoryId = "food",
        merchant = "Coffee Shop",
        note = "Team breakfast",
        occurredAt = TestClock.November14,
        createdAt = TestClock.CreatedAt,
        updatedAt = TestClock.UpdatedAt,
    )
}
