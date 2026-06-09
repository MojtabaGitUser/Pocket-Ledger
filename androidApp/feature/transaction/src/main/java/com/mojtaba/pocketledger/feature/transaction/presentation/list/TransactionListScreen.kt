package com.mojtaba.pocketledger.feature.transaction.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mojtaba.pocketledger.core.designsystem.accessibility.pocketLedgerHeading
import com.mojtaba.pocketledger.core.designsystem.component.AdaptiveContainer
import com.mojtaba.pocketledger.core.designsystem.component.EmptyState
import com.mojtaba.pocketledger.core.designsystem.component.ErrorState
import com.mojtaba.pocketledger.core.designsystem.component.LoadingState
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    uiState: TransactionListUiState,
    onAction: (TransactionListAction) -> Unit,
    modifier: Modifier = Modifier,
    selectedTransactionId: String? = null,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transactions",
                        modifier = Modifier.pocketLedgerHeading(),
                    )
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { contentPadding ->
        AdaptiveContainer(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            maxWidth = 1040.dp,
        ) {
            TransactionListContent(
                uiState = uiState,
                onAction = onAction,
                selectedTransactionId = selectedTransactionId,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun TransactionListContent(
    uiState: TransactionListUiState,
    onAction: (TransactionListAction) -> Unit,
    modifier: Modifier = Modifier,
    selectedTransactionId: String? = null,
) {
    when (uiState) {
        TransactionListUiState.Loading -> Centered(modifier) {
            LoadingState(message = "Loading transactions")
        }
        TransactionListUiState.Empty -> Centered(modifier) {
            EmptyState(
                title = "No transactions yet",
                message = "Saved transactions will appear here.",
            )
        }
        is TransactionListUiState.Error -> Centered(modifier) {
            ErrorState(
                title = "Could not load transactions",
                message = uiState.message,
                onRetry = { onAction(TransactionListAction.RetryClicked) },
            )
        }
        is TransactionListUiState.Content -> {
            val spacing = PocketLedgerThemeDefaults.spacing
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing.none),
            ) {
                itemsIndexed(
                    items = uiState.transactions,
                    key = { _, transaction -> transaction.id },
                ) { index, transaction ->
                    TransactionListItem(
                        transaction = transaction,
                        selected = transaction.id == selectedTransactionId,
                        onClick = {
                            onAction(TransactionListAction.TransactionClicked(transaction.id))
                        },
                        showDivider = index < uiState.transactions.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun Centered(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
