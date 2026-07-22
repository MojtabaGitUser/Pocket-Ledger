package com.mojtaba.folentra.feature.transaction.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.folentra.core.designsystem.component.EmptyState
import com.mojtaba.folentra.core.designsystem.theme.FolentraPreviewTheme
import com.mojtaba.folentra.feature.transaction.presentation.detail.TransactionDetailContent
import com.mojtaba.folentra.feature.transaction.presentation.detail.TransactionDetailUiState
import com.mojtaba.folentra.feature.transaction.presentation.detail.previewTransactionDetail
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListUiState
import com.mojtaba.folentra.feature.transaction.presentation.list.previewTransactions

@Preview(name = "Compact list", showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun TransactionAdaptiveCompactPreview() {
    FolentraPreviewTheme {
        TransactionListDetailContent(
            listUiState = TransactionListUiState.Content(previewTransactions),
            selectedTransactionId = null,
            onListAction = {},
        ) {
            NoTransactionSelectedPreviewContent()
        }
    }
}

@Preview(name = "Medium list detail", showBackground = true, widthDp = 840, heightDp = 900)
@Composable
private fun TransactionAdaptiveMediumSelectedPreview() {
    FolentraPreviewTheme {
        TransactionListDetailContent(
            listUiState = TransactionListUiState.Content(previewTransactions),
            selectedTransactionId = "transaction-1",
            onListAction = {},
        ) {
            TransactionDetailContent(
                uiState = TransactionDetailUiState.Content(previewTransactionDetail),
                onAction = {},
            )
        }
    }
}

@Preview(name = "Expanded no selection", showBackground = true, widthDp = 1280, heightDp = 900)
@Composable
private fun TransactionAdaptiveExpandedNoSelectionPreview() {
    FolentraPreviewTheme {
        TransactionListDetailContent(
            listUiState = TransactionListUiState.Content(previewTransactions),
            selectedTransactionId = null,
            onListAction = {},
        ) {
            NoTransactionSelectedPreviewContent()
        }
    }
}

@Preview(name = "Expanded empty", showBackground = true, widthDp = 1280, heightDp = 900)
@Composable
private fun TransactionAdaptiveExpandedEmptyPreview() {
    FolentraPreviewTheme {
        TransactionListDetailContent(
            listUiState = TransactionListUiState.Empty,
            selectedTransactionId = null,
            onListAction = {},
        ) {
            NoTransactionSelectedPreviewContent()
        }
    }
}

@Composable
private fun NoTransactionSelectedPreviewContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(
            title = "Select a transaction",
            message = "Choose a transaction from the list to view its details.",
        )
    }
}
