package com.mojtaba.folentra.feature.transaction.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.folentra.core.designsystem.component.AmountDisplay
import com.mojtaba.folentra.core.designsystem.component.AmountTone
import com.mojtaba.folentra.core.designsystem.theme.FolentraPreviewTheme
import com.mojtaba.folentra.feature.transaction.model.TransactionDetailUiModel

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionDetailContentPreview() {
    FolentraPreviewTheme {
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
    FolentraPreviewTheme {
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
    FolentraPreviewTheme {
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
