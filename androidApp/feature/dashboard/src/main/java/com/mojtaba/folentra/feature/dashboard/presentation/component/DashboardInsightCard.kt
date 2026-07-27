package com.mojtaba.folentra.feature.dashboard.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.mojtaba.folentra.feature.dashboard.model.DashboardInsight
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults
import com.mojtaba.folentra.feature.dashboard.presentation.model.DashboardFormatters

@Composable
fun DashboardInsightCard(
    insights: List<DashboardInsight>,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    val displayInsights = insights.ifEmpty { listOf(DashboardInsight.NoData) }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            displayInsights.forEach { insight ->
                InsightItem(insight = insight)
            }
        }
    }
}

@Composable
private fun InsightItem(
    insight: DashboardInsight,
    modifier: Modifier = Modifier,
) {
    val title = DashboardFormatters.insightTitle(insight)
    val message = DashboardFormatters.insightMessage(insight)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Insight: $title. $message"
            },
        verticalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.small),
    ) {
        SuggestionChip(
            onClick = {},
            label = { Text(title) },
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
