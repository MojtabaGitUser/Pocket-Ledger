package com.mojtaba.folentra.feature.transaction.presentation.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.folentra.core.designsystem.component.AmountDisplay
import com.mojtaba.folentra.core.designsystem.component.AmountTone
import com.mojtaba.folentra.core.designsystem.theme.FolentraPreviewTheme
import com.mojtaba.folentra.feature.transaction.model.TransactionListItemUiModel

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionListLoadingPreview() {
    FolentraPreviewTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Loading,
            onAction = {},
            onAddTransaction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionListEmptyPreview() {
    FolentraPreviewTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Empty,
            onAction = {},
            onAddTransaction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionListContentPreview() {
    FolentraPreviewTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Content(previewTransactions),
            onAction = {},
            onAddTransaction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionListErrorPreview() {
    FolentraPreviewTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Error("The local ledger could not be read."),
            onAction = {},
            onAddTransaction = {},
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
