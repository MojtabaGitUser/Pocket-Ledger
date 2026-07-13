package com.mojtaba.pocketledger.debughealth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.mojtaba.pocketledger.PocketLedgerAppGraph
import com.mojtaba.pocketledger.core.designsystem.accessibility.pocketLedgerHeading
import com.mojtaba.pocketledger.core.designsystem.component.AdaptiveContainer
import com.mojtaba.pocketledger.core.designsystem.component.SectionHeader
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults

@Composable
fun DebugHealthScreen(
    appGraph: PocketLedgerAppGraph,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    val factory = remember(context, appGraph.featureFlags, appGraph.productAnalyticsProviderState, appGraph.backgroundTaskScheduler) {
        DebugHealthReportFactory.from(
            context = context,
            featureFlags = appGraph.featureFlags,
            analyticsProviderState = appGraph.productAnalyticsProviderState,
            backgroundTaskScheduler = appGraph.backgroundTaskScheduler,
        )
    }
    val report = produceState<DebugHealthReport?>(initialValue = null, factory) {
        value = factory.create()
    }.value

    DebugHealthContent(
        report = report ?: DebugHealthReport(emptyList()),
        modifier = modifier,
    )
}

@Composable
fun DebugHealthContent(
    report: DebugHealthReport,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    AdaptiveContainer(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            item {
                SectionHeader(
                    title = "Debug health",
                    subtitle = "Safe development, CI/CD, and release readiness diagnostics",
                )
            }
            items(report.sections) { section ->
                DebugHealthSectionCard(section = section)
            }
        }
    }
}

@Composable
private fun DebugHealthSectionCard(
    section: DebugHealthSection,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.pocketLedgerHeading(),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            section.statuses.forEach { status ->
                DebugHealthStatusRow(status = status)
            }
        }
    }
}

@Composable
private fun DebugHealthStatusRow(
    status: DebugHealthStatus,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing
    val colors = CardDefaults.cardColors(
        containerColor = when (status.severity) {
            DebugHealthSeverity.Ready -> MaterialTheme.colorScheme.primaryContainer
            DebugHealthSeverity.Warning -> MaterialTheme.colorScheme.errorContainer
            DebugHealthSeverity.Neutral -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = when (status.severity) {
            DebugHealthSeverity.Ready -> MaterialTheme.colorScheme.onPrimaryContainer
            DebugHealthSeverity.Warning -> MaterialTheme.colorScheme.onErrorContainer
            DebugHealthSeverity.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = status.label
                stateDescription = status.accessibilityState
            },
        colors = colors,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        ) {
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = status.value,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = status.description,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
