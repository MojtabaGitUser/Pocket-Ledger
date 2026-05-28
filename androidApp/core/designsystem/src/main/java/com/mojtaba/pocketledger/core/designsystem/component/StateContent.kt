package com.mojtaba.pocketledger.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    StateColumn(
        title = title,
        message = message,
        modifier = modifier,
        icon = icon,
        action = action,
    )
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    Column(
        modifier = modifier
            .padding(spacing.large)
            .semantics {
                contentDescription = message ?: "Loading"
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        CircularProgressIndicator()
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String = "Retry",
    onRetry: (() -> Unit)? = null,
) {
    StateColumn(
        title = title,
        message = message,
        modifier = modifier.semantics {
            liveRegion = LiveRegionMode.Polite
        },
        action = if (onRetry != null) {
            {
                Button(onClick = onRetry) {
                    Text(text = retryLabel)
                }
            }
        } else {
            null
        },
    )
}

@Composable
private fun StateColumn(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    Column(
        modifier = modifier
            .padding(spacing.large)
            .semantics {
                contentDescription = "$title. $message"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        if (icon != null) {
            icon()
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            action()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    PocketLedgerPreviewTheme {
        EmptyState(
            title = "No transactions yet",
            message = "Add your first transaction to start tracking your ledger.",
            action = {
                Button(onClick = {}) {
                    Text(text = "Add transaction")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    PocketLedgerPreviewTheme {
        LoadingState(message = "Loading transactions")
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    PocketLedgerPreviewTheme {
        ErrorState(
            title = "Could not load",
            message = "Check your connection and try again.",
            onRetry = {},
        )
    }
}
