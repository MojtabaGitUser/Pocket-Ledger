package com.mojtaba.pocketledger.feature.dashboard.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mojtaba.pocketledger.core.designsystem.component.AmountDisplay
import com.mojtaba.pocketledger.core.designsystem.component.AmountText
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults

@Composable
fun DashboardMetricCard(
    label: String,
    amount: AmountDisplay,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    ElevatedCard(
        modifier = modifier.semantics {
            contentDescription = listOfNotNull(label, amount.contentDescription, supportingText)
                .joinToString(separator = ", ")
        },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AmountText(
                amount = amount,
                style = MaterialTheme.typography.headlineSmall,
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
