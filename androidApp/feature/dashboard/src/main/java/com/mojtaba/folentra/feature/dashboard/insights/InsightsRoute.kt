package com.mojtaba.folentra.feature.dashboard.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mojtaba.folentra.core.ai.AiProviderType
import com.mojtaba.folentra.core.data.repository.BudgetRepository
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.designsystem.accessibility.folentraHeading
import com.mojtaba.folentra.core.designsystem.component.AdaptiveContainer
import com.mojtaba.folentra.core.designsystem.component.EmptyState
import com.mojtaba.folentra.core.designsystem.component.ErrorState
import com.mojtaba.folentra.core.designsystem.component.LoadingState
import com.mojtaba.folentra.core.designsystem.component.SectionHeader
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults

@Composable
fun InsightsRoute(
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    aiFallbackStrategy: AiFallbackStrategy,
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = viewModel(
        factory = InsightsViewModelFactory(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            aiFallbackStrategy = aiFallbackStrategy,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    InsightsScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onAction: (InsightsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Insights",
                        modifier = Modifier.folentraHeading(),
                    )
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { contentPadding ->
        AdaptiveContainer(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            maxWidth = 1040.dp,
        ) {
            when (uiState) {
                InsightsUiState.Loading -> Centered(Modifier.fillMaxSize()) {
                    LoadingState(message = "Generating private insights")
                }
                is InsightsUiState.Empty -> Centered(Modifier.fillMaxSize()) {
                    EmptyState(
                        title = "No monthly activity",
                        message = "Add transactions for ${uiState.periodLabel} to generate local insights.",
                    )
                }
                is InsightsUiState.Error -> Centered(Modifier.fillMaxSize()) {
                    ErrorState(
                        title = "Could not generate insights",
                        message = uiState.message,
                        onRetry = { onAction(InsightsAction.RetryClicked) },
                    )
                }
                is InsightsUiState.Content -> InsightsContent(
                    uiState = uiState,
                    onRetry = { onAction(InsightsAction.RetryClicked) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun InsightsContent(
    uiState: InsightsUiState.Content,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        item {
            SectionHeader(
                title = "Monthly summary",
                subtitle = uiState.periodLabel,
                actionLabel = "Regenerate",
                onActionClick = onRetry,
                modifier = Modifier
                    .padding(top = spacing.medium)
                    .semantics { heading() },
            )
        }
        item {
            ProviderStatusCard(uiState)
        }
        item {
            MetricRow(uiState)
        }
        item {
            TextCard(
                title = uiState.result.title,
                body = uiState.result.summaryText,
                contentDescription = "Monthly summary. ${uiState.result.summaryText}",
            )
        }
        item {
            ListCard(title = "Insights", items = uiState.result.insights.ifEmpty { listOf("No notable local patterns found.") })
        }
        if (uiState.result.warnings.isNotEmpty()) {
            item { ListCard(title = "Warnings", items = uiState.result.warnings) }
        }
        item {
            ListCard(title = "Suggested actions", items = uiState.result.suggestedActions)
        }
        item {
            Text(
                text = "Insights are generated on this device using local data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(bottom = spacing.medium)
                    .semantics { contentDescription = "Privacy note. Insights are generated on this device using local data." },
            )
        }
    }
}

@Composable
private fun ProviderStatusCard(uiState: InsightsUiState.Content) {
    val label = when (uiState.result.providerType) {
        AiProviderType.GeminiNano,
        AiProviderType.MlKit,
        -> "On-device AI"
        AiProviderType.RuleBased -> "Rule-based fallback"
        AiProviderType.NoOp -> "Unavailable"
    }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                stateDescription = label
                contentDescription = "Insights provider status: $label"
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FolentraThemeDefaults.spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.extraSmall)) {
                Text("Provider", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
            AssistChip(onClick = {}, label = { Text(if (uiState.isFallback) "Fallback" else "Local") })
        }
    }
}

@Composable
private fun MetricRow(uiState: InsightsUiState.Content) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.medium),
    ) {
        MetricCard("Income", uiState.incomeText, Modifier.weight(1f))
        MetricCard("Expense", uiState.expenseText, Modifier.weight(1f))
        MetricCard("Net", uiState.netText, Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.semantics { contentDescription = "$title $value" }) {
        Column(
            modifier = Modifier.padding(FolentraThemeDefaults.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.extraSmall),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun TextCard(title: String, body: String, contentDescription: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().semantics { this.contentDescription = contentDescription }) {
        Column(
            modifier = Modifier.padding(FolentraThemeDefaults.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.small),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ListCard(title: String, items: List<String>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(FolentraThemeDefaults.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.small),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            items.forEachIndexed { index, item ->
                Text(
                    text = "${index + 1}. $item",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics { contentDescription = "$title item ${index + 1}. $item" },
                )
            }
        }
    }
}

@Composable
private fun Centered(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        content()
    }
}

private class InsightsViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val aiFallbackStrategy: AiFallbackStrategy,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(InsightsViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return InsightsViewModel(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            aiFallbackStrategy = aiFallbackStrategy,
        ) as T
    }
}
