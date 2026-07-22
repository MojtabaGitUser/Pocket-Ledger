package com.mojtaba.folentra.desktop.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun DesktopInsightsScreen(
    state: DesktopInsightsUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (state) {
            DesktopInsightsUiState.Loading -> CenteredState {
                LoadingState("Generating private desktop insights")
            }
            is DesktopInsightsUiState.Empty -> CenteredState {
                EmptyState(
                    title = "No monthly activity",
                    message = "Add sample or local transactions for ${state.periodLabel} to generate local insights.",
                )
            }
            is DesktopInsightsUiState.Error -> CenteredState {
                ErrorState(message = state.message)
            }
            is DesktopInsightsUiState.Content -> DesktopInsightsContent(state)
        }
    }
}

@Composable
private fun DesktopInsightsContent(state: DesktopInsightsUiState.Content) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        val twoColumn = maxWidth >= 1040.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Header(state)
            MetricOverview(state)
            if (twoColumn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1.05f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SummaryCard(state)
                        TopCategoriesCard(state.topCategories)
                    }
                    Column(
                        modifier = Modifier.weight(0.95f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ProviderStatusCard(state.result.providerStatus)
                        ListCard("Insight cards", state.result.insights)
                        OptionalListCard("Suggested actions", state.result.suggestedActions)
                        OptionalListCard("Observations", state.result.warnings)
                    }
                }
            } else {
                SummaryCard(state)
                ProviderStatusCard(state.result.providerStatus)
                TopCategoriesCard(state.topCategories)
                ListCard("Insight cards", state.result.insights)
                OptionalListCard("Suggested actions", state.result.suggestedActions)
                OptionalListCard("Observations", state.result.warnings)
            }
            Text(
                text = "Desktop demo uses sample aggregate data and local rule-based insight generation only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics {
                    contentDescription = "Privacy note. Desktop demo uses sample aggregate data and local rule-based insight generation only."
                },
            )
        }
    }
}

@Composable
private fun Header(state: DesktopInsightsUiState.Content) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = "Monthly summary for ${state.periodLabel}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MetricOverview(state: DesktopInsightsUiState.Content) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= 760.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricCard("Income", state.incomeText, Modifier.weight(1f))
                MetricCard("Expense", state.expenseText, Modifier.weight(1f))
                MetricCard("Net", state.netText, Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Income", state.incomeText)
                MetricCard("Expense", state.expenseText)
                MetricCard("Net", state.netText)
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
            .heightIn(min = 104.dp)
            .semantics { contentDescription = "$label $value" },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SummaryCard(state: DesktopInsightsUiState.Content) {
    InsightPanel(
        title = state.result.title,
        semanticDescription = "Monthly summary. ${state.result.summaryText}",
    ) {
        Text(
            text = state.result.summaryText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ProviderStatusCard(status: DesktopProviderStatus) {
    val label = when (status) {
        DesktopProviderStatus.RuleBasedFallback -> "Rule-based fallback"
        DesktopProviderStatus.LocalOnDevice -> "On-device AI"
        DesktopProviderStatus.Unavailable -> "Unavailable"
    }
    InsightPanel(
        title = "Provider status",
        semanticDescription = "Insights provider status: $label",
        stateDescription = label,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            AssistChip(onClick = {}, label = { Text("Local only") })
        }
    }
}

@Composable
private fun TopCategoriesCard(categories: List<DesktopCategorySummary>) {
    InsightPanel(
        title = "Top spending groups",
        semanticDescription = "Top spending groups",
    ) {
        if (categories.isEmpty()) {
            Text("No spending groups for this period.", style = MaterialTheme.typography.bodyMedium)
        } else {
            val max = categories.maxOf { it.totalExpenseMinor }.coerceAtLeast(1L)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.forEach { category ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(category.label, style = MaterialTheme.typography.bodyMedium)
                            Text("${category.transactionCount} sample tx", style = MaterialTheme.typography.bodySmall)
                        }
                        LinearProgressIndicator(
                            progress = { category.totalExpenseMinor.toFloat() / max.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .semantics {
                                    stateDescription = "${category.label} relative spending group"
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionalListCard(title: String, items: List<String>) {
    if (items.isNotEmpty()) {
        ListCard(title, items)
    }
}

@Composable
private fun ListCard(title: String, items: List<String>) {
    InsightPanel(title = title, semanticDescription = title) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.ifEmpty { listOf("No notable local patterns found.") }.forEachIndexed { index, item ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${index + 1}.", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = "$title item ${index + 1}. $item"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightPanel(
    title: String,
    semanticDescription: String,
    modifier: Modifier = Modifier,
    stateDescription: String? = null,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().semantics {
            contentDescription = semanticDescription
            if (stateDescription != null) {
                this.stateDescription = stateDescription
            }
        },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
            content()
        }
    }
}

@Composable
private fun CenteredState(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
private fun LoadingState(message: String) {
    Column(
        modifier = Modifier.padding(24.dp).semantics {
            contentDescription = message
            stateDescription = "Loading"
            liveRegion = LiveRegionMode.Polite
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LinearProgressIndicator(modifier = Modifier.width(240.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    Column(
        modifier = Modifier.padding(24.dp).semantics {
            contentDescription = "$title. $message"
            stateDescription = "Empty"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier.padding(24.dp).semantics {
            contentDescription = "Could not generate insights. $message"
            stateDescription = "Error"
            liveRegion = LiveRegionMode.Polite
            error(message)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Could not generate insights", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = {}) {
            Text("Retry")
        }
    }
}


