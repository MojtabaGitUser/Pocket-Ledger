package com.mojtaba.folentra.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.folentra.core.designsystem.component.AdaptiveContainer
import com.mojtaba.folentra.core.designsystem.component.EmptyState
import com.mojtaba.folentra.core.designsystem.component.SectionHeader
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults

@Composable
fun DashboardPlaceholderScreen() {
    PlaceholderDestinationScreen(
        title = "Dashboard",
        subtitle = "Overview shell",
        body = "Temporary dashboard destination until a feature module owns this screen.",
    )
}

@Composable
fun TransactionsPlaceholderScreen() {
    PlaceholderDestinationScreen(
        title = "Transactions",
        subtitle = "Ledger activity",
        body = "Temporary transactions destination until transaction feature work is implemented.",
    )
}

@Composable
fun SearchPlaceholderScreen() {
    PlaceholderDestinationScreen(
        title = "Search",
        subtitle = "Find ledger entries",
        body = "Temporary search destination until search behavior is implemented.",
    )
}

@Composable
fun InsightsPlaceholderScreen() {
    PlaceholderDestinationScreen(
        title = "Insights",
        subtitle = "Spending trends",
        body = "Temporary insights destination until analytics feature work is implemented.",
    )
}

@Composable
fun SettingsPlaceholderScreen() {
    PlaceholderDestinationScreen(
        title = "Settings",
        subtitle = "App preferences",
        body = "Temporary settings destination until preferences are implemented.",
    )
}


@Composable
private fun PlaceholderDestinationScreen(
    title: String,
    subtitle: String,
    body: String,
) {
    val spacing = FolentraThemeDefaults.spacing

    AdaptiveContainer(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            SectionHeader(
                title = title,
                subtitle = subtitle,
            )
            EmptyState(
                title = "$title placeholder",
                message = body,
            )
            Text(
                text = "TODO: Move this destination into its feature module when that module exists.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
