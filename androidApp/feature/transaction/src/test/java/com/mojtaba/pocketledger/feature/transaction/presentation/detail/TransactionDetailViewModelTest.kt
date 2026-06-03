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
    fun deleteActionEmitsEventAndDoesNotDelete() = runTest {
        val transactionRepository = TestTransactionRepository(listOf(testTransaction()))
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            transactionId = "transaction-1",
        )
        val events = mutableListOf<TransactionDetailEvent>()
        val job = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(TransactionDetailAction.DeleteClicked)
        advanceUntilIdle()

        assertEquals(TransactionDetailEvent.DeleteRequested("transaction-1"), events.single())
        assertEquals(0, transactionRepository.deleteByIdCalls)
        job.cancel()
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
