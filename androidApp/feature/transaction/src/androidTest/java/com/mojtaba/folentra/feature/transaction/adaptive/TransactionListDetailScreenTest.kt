package com.mojtaba.folentra.feature.transaction.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mojtaba.folentra.core.designsystem.component.EmptyState
import com.mojtaba.folentra.core.designsystem.theme.FolentraTheme
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListAction
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListUiState
import com.mojtaba.folentra.feature.transaction.presentation.list.previewTransactions
import org.junit.Rule
import org.junit.Test

class TransactionListDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun noSelectionStartsInListPaneOnCompactWidth() {
        setContent(
            listUiState = TransactionListUiState.Content(previewTransactions),
            selectedTransactionId = null,
        ) {
            EmptyState(
                title = "Select a transaction",
                message = "Choose a transaction from the list to view its details.",
                modifier = Modifier.semantics {
                    contentDescription = "No transaction selected"
                },
            )
        }

        composeRule.onNodeWithText("Coffee Shop").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Transaction list pane").assertIsDisplayed()
    }

    @Test
    fun emptyListStateIsDisplayedInListPane() {
        setContent(
            listUiState = TransactionListUiState.Empty,
            selectedTransactionId = null,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Transaction detail pane" },
            ) {
                Text("Select a transaction")
            }
        }

        composeRule.onNodeWithText("No transactions yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add an expense or income to start tracking your ledger.").assertIsDisplayed()
    }

    @Test
    fun selectingTransactionUpdatesDetailPane() {
        var selectedTransactionId by mutableStateOf<String?>(null)
        composeRule.setContent {
            FolentraTheme(dynamicColor = false) {
                TransactionListDetailContent(
                    listUiState = TransactionListUiState.Content(previewTransactions),
                    selectedTransactionId = selectedTransactionId,
                    onListAction = { action ->
                        if (action is TransactionListAction.TransactionClicked) {
                            selectedTransactionId = action.transactionId
                        }
                    },
                ) {
                    Text(
                        text = selectedTransactionId?.let { "Detail for $it" } ?: "Select a transaction",
                        modifier = Modifier.semantics {
                            contentDescription = "Transaction detail pane"
                        },
                    )
                }
            }
        }

        composeRule.onNodeWithText("Coffee Shop").performClick()

        composeRule.onNodeWithText("Detail for transaction-1").assertIsDisplayed()
    }

    private fun setContent(
        listUiState: TransactionListUiState,
        selectedTransactionId: String?,
        detailPane: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            FolentraTheme(dynamicColor = false) {
                TransactionListDetailContent(
                    listUiState = listUiState,
                    selectedTransactionId = selectedTransactionId,
                    onListAction = {},
                    detailPane = detailPane,
                )
            }
        }
    }
}
