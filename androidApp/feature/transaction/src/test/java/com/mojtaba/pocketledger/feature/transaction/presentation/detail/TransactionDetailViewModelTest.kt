package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.mojtaba.pocketledger.feature.transaction.navigation.TransactionRoutes
import com.mojtaba.pocketledger.feature.transaction.testing.MainDispatcherRule
import com.mojtaba.pocketledger.feature.transaction.testing.TestCategoryRepository
import com.mojtaba.pocketledger.feature.transaction.testing.TestTagRepository
import com.mojtaba.pocketledger.feature.transaction.testing.TestTransactionRepository
import com.mojtaba.pocketledger.feature.transaction.testing.testTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            transactionRepository = TestTransactionRepository(listOf(testTransaction())),
            transactionId = "transaction-1",
        )
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TransactionDetailUiState.Content)
        assertEquals("-\$42.50", (state as TransactionDetailUiState.Content).transaction.amount.text)
        assertEquals("Food", state.transaction.categoryLabel)
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
            transactionRepository = TestTransactionRepository(listOf(testTransaction())),
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
        val transactionRepository = TestTransactionRepository(listOf(testTransaction()))
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
        val transactionRepository = TestTransactionRepository(listOf(testTransaction()))
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
        val transactionRepository = TestTransactionRepository(listOf(testTransaction()))
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
        val transactionRepository = TestTransactionRepository(listOf(testTransaction()))
        val tagRepository = TestTagRepository(initialLinks = mapOf("transaction-1" to setOf("work")))
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
        val transactionRepository = TestTransactionRepository(listOf(testTransaction()))
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
        val transactionRepository = TestTransactionRepository(listOf(testTransaction())).apply {
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
        assertTrue(
            events.contains(TransactionDetailEvent.ShowDeleteFailedSnackbar("Delete failed")),
        )
        assertTrue(transactionRepository.containsTransaction("transaction-1"))
        uiJob.cancel()
        eventJob.cancel()
    }

    private fun newViewModel(
        transactionRepository: TestTransactionRepository = TestTransactionRepository(),
        categoryRepository: TestCategoryRepository = TestCategoryRepository(),
        tagRepository: TestTagRepository = TestTagRepository(),
        transactionId: String,
    ): TransactionDetailViewModel = TransactionDetailViewModel(
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        tagRepository = tagRepository,
        savedStateHandle = SavedStateHandle(
            mapOf(TransactionRoutes.TransactionIdArg to transactionId),
        ),
    )
}
