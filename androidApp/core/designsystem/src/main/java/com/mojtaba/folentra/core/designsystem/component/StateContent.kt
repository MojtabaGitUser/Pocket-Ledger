package com.mojtaba.folentra.core.designsystem.component

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
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.folentra.core.designsystem.accessibility.folentraHeading
import com.mojtaba.folentra.core.designsystem.preview.PreviewText
import com.mojtaba.folentra.core.designsystem.theme.FolentraPreviewTheme
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults

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
        stateDescription = "Empty",
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
    val spacing = FolentraThemeDefaults.spacing

    Column(
        modifier = modifier
            .padding(spacing.large)
            .semantics {
                contentDescription = message ?: "Loading"
                stateDescription = "Loading"
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
        stateDescription = "Error",
        modifier = modifier.semantics {
            liveRegion = LiveRegionMode.Polite
            error(message)
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
    stateDescription: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val spacing = FolentraThemeDefaults.spacing

    Column(
        modifier = modifier
            .padding(spacing.large)
            .semantics {
                contentDescription = "$title. $message"
                this.stateDescription = stateDescription
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
            modifier = Modifier.folentraHeading(),
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
    FolentraPreviewTheme {
        EmptyState(
            title = PreviewText.emptyMessage,
            message = "Add a transaction to start tracking your ledger.",
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
    FolentraPreviewTheme {
        LoadingState(message = PreviewText.loadingMessage)
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    FolentraPreviewTheme {
        ErrorState(
            title = "Could not load",
            message = PreviewText.errorMessage,
            onRetry = {},
        )
    }
}
