package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mojtaba.pocketledger.core.designsystem.component.AdaptiveContainer
import com.mojtaba.pocketledger.core.designsystem.component.AmountText
import com.mojtaba.pocketledger.core.designsystem.component.EmptyState
import com.mojtaba.pocketledger.core.designsystem.component.ErrorState
import com.mojtaba.pocketledger.core.designsystem.component.LoadingState
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.transaction.model.TransactionDetailUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    uiState: TransactionDetailUiState,
    onAction: (TransactionDetailAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Detail") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onAction(TransactionDetailAction.EditClicked) },
                        enabled = uiState is TransactionDetailUiState.Content,
                    ) {
                        Text("Edit")
                    }
                    TextButton(
                        onClick = { onAction(TransactionDetailAction.DeleteClicked) },
                        enabled = uiState is TransactionDetailUiState.Content,
                    ) {
                        Text("Delete")
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { contentPadding ->
        AdaptiveContainer(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            maxWidth = 840.dp,
        ) {
            TransactionDetailContent(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun TransactionDetailContent(
    uiState: TransactionDetailUiState,
    onAction: (TransactionDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        TransactionDetailUiState.Loading -> Centered(modifier) {
            LoadingState(message = "Loading transaction")
        }
        TransactionDetailUiState.NotFound -> Centered(modifier) {
            EmptyState(
                title = "Transaction not found",
                message = "This transaction may have been deleted.",
            )
        }
        is TransactionDetailUiState.Error -> Centered(modifier) {
            ErrorState(
                title = "Could not load transaction",
                message = uiState.message,
                onRetry = { onAction(TransactionDetailAction.RetryClicked) },
            )
        }
        is TransactionDetailUiState.Content -> TransactionDetailCard(
            transaction = uiState.transaction,
            modifier = modifier.padding(PocketLedgerThemeDefaults.spacing.medium),
        )
    }
}

@Composable
private fun TransactionDetailCard(
    transaction: TransactionDetailUiModel,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                AmountText(
                    amount = transaction.amount,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = transaction.typeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DetailRow(label = "Category", value = transaction.categoryLabel)
            DetailRow(label = "Date", value = transaction.dateLabel)
            transaction.merchantLabel?.let { DetailRow(label = "Merchant", value = it) }
            transaction.noteLabel?.let { DetailRow(label = "Note", value = it, singleLine = false) }
            if (transaction.tagLabels.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    ) {
                        items(transaction.tagLabels) { tag ->
                            AssistChip(
                                onClick = {},
                                label = { Text(tag) },
                            )
                        }
                    }
                }
            }
            DetailRow(label = "Created", value = transaction.createdAtLabel)
            DetailRow(label = "Updated", value = transaction.updatedAtLabel)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PocketLedgerThemeDefaults.spacing.medium),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.32f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip,
            modifier = Modifier.weight(0.68f),
        )
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
