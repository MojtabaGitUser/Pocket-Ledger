package com.mojtaba.pocketledger.feature.transaction.presentation.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.component.AmountDisplay
import com.mojtaba.pocketledger.core.designsystem.component.AmountTone
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import com.mojtaba.pocketledger.feature.transaction.model.TransactionListItemUiModel

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionListLoadingPreview() {
    PocketLedgerPreviewTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Loading,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionListEmptyPreview() {
    PocketLedgerPreviewTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Empty,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionListContentPreview() {
    PocketLedgerPreviewTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Content(previewTransactions),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionListErrorPreview() {
    PocketLedgerPreviewTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Error("The local ledger could not be read."),
            onAction = {},
        )
    }
}

internal val previewTransactions = listOf(
    TransactionListItemUiModel(
        id = "transaction-1",
        amount = AmountDisplay("-\$42.50", AmountTone.Negative),
        typeLabel = "Expense",
        categoryLabel = "Food",
        dateLabel = "Nov 14, 2023",
        title = "Coffee Shop",
        notePreview = "Team breakfast",
        tagLabels = listOf("Work"),
        tone = AmountTone.Negative,
    ),
    TransactionListItemUiModel(
        id = "transaction-2",
        amount = AmountDisplay("+\$2,400.00", AmountTone.Positive),
        typeLabel = "Income",
        categoryLabel = "Salary",
        dateLabel = "Nov 15, 2023",
        title = "Monthly paycheck",
        notePreview = null,
        tagLabels = emptyList(),
        tone = AmountTone.Positive,
    ),
)
