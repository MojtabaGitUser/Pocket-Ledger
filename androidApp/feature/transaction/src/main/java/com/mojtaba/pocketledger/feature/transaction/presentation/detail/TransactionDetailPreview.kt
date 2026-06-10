package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.component.AmountDisplay
import com.mojtaba.pocketledger.core.designsystem.component.AmountTone
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import com.mojtaba.pocketledger.feature.transaction.model.TransactionDetailUiModel

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionDetailContentPreview() {
    PocketLedgerPreviewTheme {
        TransactionDetailScreen(
            uiState = TransactionDetailUiState.Content(previewTransactionDetail),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionDetailNotFoundPreview() {
    PocketLedgerPreviewTheme {
        TransactionDetailScreen(
            uiState = TransactionDetailUiState.NotFound,
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionDetailErrorPreview() {
    PocketLedgerPreviewTheme {
        TransactionDetailScreen(
            uiState = TransactionDetailUiState.Error("The local transaction could not be read."),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

internal val previewTransactionDetail = TransactionDetailUiModel(
    id = "transaction-1",
    amount = AmountDisplay("-\$42.50", AmountTone.Negative),
    typeLabel = "Expense",
    categoryLabel = "Food",
    dateLabel = "Nov 14, 2023",
    merchantLabel = "Coffee Shop",
    noteLabel = "Team breakfast",
    tagLabels = listOf("Work", "Client"),
    createdAtLabel = "Nov 14, 2023, 9:00 AM",
    updatedAtLabel = "Nov 14, 2023, 9:15 AM",
)
