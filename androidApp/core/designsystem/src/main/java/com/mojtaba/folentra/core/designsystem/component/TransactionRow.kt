package com.mojtaba.folentra.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.folentra.core.designsystem.preview.PreviewTransactions
import com.mojtaba.folentra.core.designsystem.theme.FolentraPreviewTheme
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults

@Composable
fun TransactionRow(
    title: String,
    amount: AmountDisplay,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    category: String? = null,
    onClick: (() -> Unit)? = null,
    onClickLabel: String = "Open transaction details",
    showDivider: Boolean = true,
    contentDescription: String = transactionRowContentDescription(
        title = title,
        subtitle = subtitle,
        category = category,
        amount = amount,
    ),
) {
    val spacing = FolentraThemeDefaults.spacing
    val highFontScale = LocalDensity.current.fontScale >= 2f
    val rowModifier = if (onClick != null) {
        modifier.clickable(
            onClickLabel = onClickLabel,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        modifier
    }

    Column(
        modifier = rowModifier
            .fillMaxWidth()
            .semantics {
                this.contentDescription = contentDescription
                if (onClick != null) {
                    role = Role.Button
                }
            },
    ) {
        if (highFontScale) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Clip,
                )
                val supportingText = listOfNotNull(category, subtitle)
                    .joinToString(separator = " - ")
                if (supportingText.isNotBlank()) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Clip,
                    )
                }
                AmountText(amount = amount)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val supportingText = listOfNotNull(category, subtitle)
                        .joinToString(separator = " - ")
                    if (supportingText.isNotBlank()) {
                        Text(
                            text = supportingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                AmountText(amount = amount)
            }
        }
        if (showDivider) {
            HorizontalDivider()
        }
    }
}

private fun transactionRowContentDescription(
    title: String,
    subtitle: String?,
    category: String?,
    amount: AmountDisplay,
): String = listOfNotNull(
    title,
    category,
    subtitle,
    amount.contentDescription,
).joinToString(separator = ", ")

@Preview(showBackground = true)
@Composable
private fun TransactionRowPreview() {
    FolentraPreviewTheme {
        val transaction = PreviewTransactions.expense
        TransactionRow(
            title = transaction.title,
            category = transaction.category,
            subtitle = transaction.subtitle,
            amount = transaction.amount,
            onClick = {},
        )
    }
}
