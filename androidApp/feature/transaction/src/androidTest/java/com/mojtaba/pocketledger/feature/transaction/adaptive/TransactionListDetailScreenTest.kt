package com.mojtaba.pocketledger.feature.transaction.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.mojtaba.pocketledger.core.designsystem.component.EmptyState
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import com.mojtaba.pocketledger.feature.transaction.presentation.list.TransactionListUiState
import com.mojtaba.pocketledger.feature.transaction.presentation.list.previewTransactions
import org.junit.Rule
import org.junit.Test

class TransactionListDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun noSelectionStateIsDisplayedInDetailPane() {
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

        composeRule.onNodeWithText("Select a transaction").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("No transaction selected").assertIsDisplayed()
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
        composeRule.onNodeWithText("Saved transactions will appear here.").assertIsDisplayed()
    }

    private fun setContent(
        listUiState: TransactionListUiState,
        selectedTransactionId: String?,
        detailPane: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
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
