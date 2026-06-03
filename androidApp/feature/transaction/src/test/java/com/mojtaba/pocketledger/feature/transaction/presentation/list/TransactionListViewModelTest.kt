package com.mojtaba.pocketledger.feature.transaction.presentation.list

import com.mojtaba.pocketledger.feature.transaction.testing.MainDispatcherRule
import com.mojtaba.pocketledger.feature.transaction.testing.TestCategoryRepository
import com.mojtaba.pocketledger.feature.transaction.testing.TestTagRepository
import com.mojtaba.pocketledger.feature.transaction.testing.TestTransactionRepository
import com.mojtaba.pocketledger.feature.transaction.testing.testCategory
import com.mojtaba.pocketledger.feature.transaction.testing.testTag
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
class TransactionListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun emitsEmptyStateWhenRepositoryHasNoTransactions() = runTest {
        val viewModel = newViewModel(transactionRepository = TestTransactionRepository())
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertEquals(TransactionListUiState.Empty, viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun emitsContentStateWhenRepositoryHasTransactions() = runTest {
        val viewModel = newViewModel(
            transactionRepository = TestTransactionRepository(listOf(testTransaction())),
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
            transactionRepository = TestTransactionRepository(
                listOf(
                    testTransaction(
                        amountMinor = -4_250,
                        categoryId = "food",
                        note = "Team breakfast",
                    ),
                ),
            ),
            categoryRepository = TestCategoryRepository(listOf(testCategory("food", "Food", "expense"))),
            tagRepository = TestTagRepository(
                initialTags = listOf(testTag("work", "Work")),
                initialLinks = mapOf("transaction-1" to setOf("work")),
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

    private fun newViewModel(
        transactionRepository: TestTransactionRepository = TestTransactionRepository(),
        categoryRepository: TestCategoryRepository = TestCategoryRepository(),
        tagRepository: TestTagRepository = TestTagRepository(),
    ): TransactionListViewModel = TransactionListViewModel(
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        tagRepository = tagRepository,
    )
}
