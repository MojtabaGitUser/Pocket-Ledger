package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TransactionDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingStateIsDisplayed() {
        setContent(TransactionDetailUiState.Loading)

        composeRule.onNodeWithText("Loading transaction").assertIsDisplayed()
    }

    @Test
    fun detailDisplaysStoredFields() {
        setContent(TransactionDetailUiState.Content(previewTransactionDetail))

        composeRule.onNodeWithText("-\$42.50").assertIsDisplayed()
        composeRule.onNodeWithText("Expense").assertIsDisplayed()
        composeRule.onNodeWithText("Food").assertIsDisplayed()
        composeRule.onNodeWithText("Nov 14, 2023").assertIsDisplayed()
        composeRule.onNodeWithText("Team breakfast").assertIsDisplayed()
        composeRule.onNodeWithText("Work").assertIsDisplayed()
    }

    @Test
    fun notFoundStateIsSafe() {
        setContent(TransactionDetailUiState.NotFound)

        composeRule.onNodeWithText("Transaction not found").assertIsDisplayed()
        composeRule.onNodeWithText("This transaction may have been deleted.").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Delete transaction").assertIsNotEnabled()
    }

    @Test
    fun errorStateDisplaysMessageAndRetryAction() {
        val harness = setContent(TransactionDetailUiState.Error("Local transaction unavailable"))

        composeRule.onNodeWithText("Could not load transaction").assertIsDisplayed()
        composeRule.onNodeWithText("Local transaction unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assertTrue(harness.retryClicked)
    }

    @Test
    fun editActionIsEmittedFromContentState() {
        val harness = setContent(TransactionDetailUiState.Content(previewTransactionDetail))

        composeRule.onNodeWithText("Edit").performClick()

        assertTrue(harness.editClicked)
    }

    @Test
    fun deleteActionOpensConfirmationDialog() {
        setContent(TransactionDetailUiState.Content(previewTransactionDetail))

        composeRule.onNodeWithContentDescription("Delete transaction").performClick()

        composeRule.onNodeWithText("Delete transaction?").assertIsDisplayed()
        composeRule.onNodeWithText("This transaction will be removed from your ledger.").assertIsDisplayed()
    }

    @Test
    fun cancelDeleteClosesConfirmationDialog() {
        val harness = setContent(TransactionDetailUiState.Content(previewTransactionDetail))

        composeRule.onNodeWithContentDescription("Delete transaction").performClick()
        composeRule.onNodeWithContentDescription("Cancel delete transaction").performClick()

        assertTrue(composeRule.onAllNodesWithText("Delete transaction?").fetchSemanticsNodes().isEmpty())
        assertFalse(harness.deleteConfirmed)
    }

    @Test
    fun confirmDeleteShowsUndoSnackbarAndUndoActionRestores() {
        val harness = setContent(TransactionDetailUiState.Content(previewTransactionDetail))

        composeRule.onNodeWithContentDescription("Delete transaction").performClick()
        composeRule.onNodeWithContentDescription("Confirm delete transaction").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Transaction deleted").assertIsDisplayed()
        composeRule.onNodeWithText("Undo").performClick()
        composeRule.waitForIdle()

        assertTrue(harness.deleteConfirmed)
        assertTrue(harness.undoClicked)
    }

    private fun setContent(uiState: TransactionDetailUiState): ScreenHarness {
        val harness = ScreenHarness(uiState)
        composeRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            PocketLedgerTheme(dynamicColor = false) {
                TransactionDetailScreen(
                    uiState = harness.uiState,
                    snackbarHostState = snackbarHostState,
                    onAction = harness::onAction,
                    onNavigateBack = {},
                )
            }
            if (harness.showDeletedSnackbar) {
                LaunchedEffect(Unit) {
                    val result = snackbarHostState.showSnackbar(
                        message = "Transaction deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Indefinite,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        harness.onAction(TransactionDetailAction.UndoDeleteClicked)
                    }
                }
            }
        }
        return harness
    }

    private class ScreenHarness(initialUiState: TransactionDetailUiState) {
        var uiState by mutableStateOf(initialUiState)
        var deleteConfirmed = false
        var editClicked = false
        var retryClicked = false
        var undoClicked = false
        var showDeletedSnackbar by mutableStateOf(false)

        fun onAction(action: TransactionDetailAction) {
            when (action) {
                TransactionDetailAction.DeleteClicked -> updateContent {
                    copy(showDeleteConfirmation = true)
                }
                TransactionDetailAction.DeleteDismissed -> updateContent {
                    copy(showDeleteConfirmation = false)
                }
                TransactionDetailAction.DeleteConfirmed -> {
                    deleteConfirmed = true
                    updateContent { copy(showDeleteConfirmation = false) }
                    showDeletedSnackbar = true
                }
                TransactionDetailAction.UndoDeleteClicked -> undoClicked = true
                TransactionDetailAction.EditClicked -> editClicked = true
                TransactionDetailAction.RetryClicked -> retryClicked = true
            }
        }

        private fun updateContent(
            reducer: TransactionDetailUiState.Content.() -> TransactionDetailUiState.Content,
        ) {
            uiState = (uiState as? TransactionDetailUiState.Content)?.reducer() ?: uiState
        }
    }
}
