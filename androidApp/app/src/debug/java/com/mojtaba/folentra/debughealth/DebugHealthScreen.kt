package com.mojtaba.folentra.debughealth

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.mojtaba.folentra.FolentraAppGraph
import com.mojtaba.folentra.core.designsystem.accessibility.folentraHeading
import com.mojtaba.folentra.core.designsystem.component.AdaptiveContainer
import com.mojtaba.folentra.core.designsystem.component.SectionHeader
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults

@Composable
fun DebugHealthScreen(
    appGraph: FolentraAppGraph,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    val factory = remember(
        context,
        appGraph.featureFlags,
        appGraph.productAnalyticsProviderState,
        appGraph.backgroundTaskScheduler,
        appGraph.crashReporter.status,
        appGraph.startupFailureReporter.status,
    ) {
        DebugHealthReportFactory.from(
            context = context,
            featureFlags = appGraph.featureFlags,
            analyticsProviderState = appGraph.productAnalyticsProviderState,
            backgroundTaskScheduler = appGraph.backgroundTaskScheduler,
            crashReportingStatus = appGraph.crashReporter.status,
            startupFailureStatus = appGraph.startupFailureReporter.status,
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
    val spacing = FolentraThemeDefaults.spacing

    AdaptiveContainer(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("DebugHealthList"),
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
    val spacing = FolentraThemeDefaults.spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.folentraHeading(),
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
    val spacing = FolentraThemeDefaults.spacing
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
