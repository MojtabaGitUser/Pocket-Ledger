package com.mojtaba.pocketledger.feature.transaction.presentation.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TransactionListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingStateIsDisplayed() {
        setContent(TransactionListUiState.Loading)

        composeRule.onNodeWithText("Loading transactions").assertIsDisplayed()
    }

    @Test
    fun emptyStateIsDisplayed() {
        setContent(TransactionListUiState.Empty)

        composeRule.onNodeWithText("No transactions yet").assertIsDisplayed()
        composeRule.onNodeWithText("Saved transactions will appear here.").assertIsDisplayed()
    }

    @Test
    fun rowDisplaysAmountCategoryDateNoteAndTags() {
        setContent(TransactionListUiState.Content(previewTransactions))

        composeRule.onNodeWithText("-\$42.50").assertIsDisplayed()
        composeRule.onNodeWithText("Food - Expense - Nov 14, 2023 - Team breakfast").assertIsDisplayed()
        composeRule.onNodeWithText("Work").assertIsDisplayed()
    }

    @Test
    fun rowClickCallsOpenDetailAction() {
        val actions = mutableListOf<TransactionListAction>()
        setContent(TransactionListUiState.Content(previewTransactions), actions::add)

        composeRule.onNodeWithText("Coffee Shop").performClick()

        assertEquals(TransactionListAction.TransactionClicked("transaction-1"), actions.single())
    }

    @Test
    fun errorStateDisplaysMessageAndRetryAction() {
        val actions = mutableListOf<TransactionListAction>()
        setContent(TransactionListUiState.Error("Local ledger unavailable"), actions::add)

        composeRule.onNodeWithText("Could not load transactions").assertIsDisplayed()
        composeRule.onNodeWithText("Local ledger unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(TransactionListAction.RetryClicked, actions.single())
    }

    private fun setContent(
        uiState: TransactionListUiState,
        onAction: (TransactionListAction) -> Unit = {},
    ) {
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
                TransactionListScreen(
                    uiState = uiState,
                    onAction = onAction,
                )
            }
        }
    }
}
