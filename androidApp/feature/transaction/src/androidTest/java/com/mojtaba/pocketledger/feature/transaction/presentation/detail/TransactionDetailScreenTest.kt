package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import org.junit.Rule
import org.junit.Test

class TransactionDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

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
    }

    private fun setContent(uiState: TransactionDetailUiState) {
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
                TransactionDetailScreen(
                    uiState = uiState,
                    onAction = {},
                    onNavigateBack = {},
                )
            }
        }
    }
}
