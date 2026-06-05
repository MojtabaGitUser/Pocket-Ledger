package com.mojtaba.pocketledger.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mojtaba.pocketledger.core.designsystem.component.AdaptiveContainer
import com.mojtaba.pocketledger.core.designsystem.component.EmptyState
import com.mojtaba.pocketledger.core.designsystem.component.ErrorState
import com.mojtaba.pocketledger.core.designsystem.component.LoadingState
import com.mojtaba.pocketledger.core.designsystem.component.SectionHeader
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardSummary
import com.mojtaba.pocketledger.feature.dashboard.presentation.component.BudgetProgressCard
import com.mojtaba.pocketledger.feature.dashboard.presentation.component.CashFlowSummaryCard
import com.mojtaba.pocketledger.feature.dashboard.presentation.component.CategorySpendChart
import com.mojtaba.pocketledger.feature.dashboard.presentation.component.DashboardEmptyState
import com.mojtaba.pocketledger.feature.dashboard.presentation.component.DashboardInsightCard
import com.mojtaba.pocketledger.feature.dashboard.presentation.component.RecentTransactionsCard

private val DashboardWideBreakpoint = 720.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { contentPadding ->
        AdaptiveContainer(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            maxWidth = 1180.dp,
        ) {
            DashboardContent(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        DashboardUiState.Loading -> Centered(modifier) {
            LoadingState(message = "Loading dashboard")
        }
        DashboardUiState.Empty -> Centered(modifier) {
            DashboardEmptyState(
                onSetBudgetClick = { onAction(DashboardAction.SetBudgetClicked) },
            )
        }
        is DashboardUiState.Error -> Centered(modifier) {
            ErrorState(
                title = "Could not load dashboard",
                message = uiState.message,
                onRetry = { onAction(DashboardAction.RetryClicked) },
            )
        }
        is DashboardUiState.Content -> DashboardSummaryContent(
            summary = uiState.summary,
            onSetBudgetClick = { onAction(DashboardAction.SetBudgetClicked) },
            modifier = modifier,
        )
    }
}

@Composable
private fun DashboardSummaryContent(
    summary: DashboardSummary,
    onSetBudgetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    BoxWithConstraints(modifier = modifier) {
        val wide = maxWidth >= DashboardWideBreakpoint
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("DashboardContentList"),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            item {
                SectionHeader(
                    title = "Financial overview",
                    subtitle = summary.period.label,
                    modifier = Modifier
                        .padding(top = spacing.medium)
                        .semantics { heading() },
                )
            }
            item {
                CashFlowSummaryCard(
                    cashFlow = summary.cashFlow,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (wide) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    ) {
                        CategorySpendChart(
                            categories = summary.topCategories,
                            modifier = Modifier.weight(1f),
                        )
                        BudgetProgressCard(
                            budgets = summary.budgetProgress,
                            onSetBudgetClick = onSetBudgetClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    ) {
                        DashboardInsightCard(
                            insights = summary.insights,
                            modifier = Modifier.weight(1f),
                        )
                        RecentTransactionsCard(
                            transactions = summary.recentTransactions,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                item {
                    CategorySpendChart(
                        categories = summary.topCategories,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    BudgetProgressCard(
                        budgets = summary.budgetProgress,
                        onSetBudgetClick = onSetBudgetClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    DashboardInsightCard(
                        insights = summary.insights,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    RecentTransactionsCard(
                        transactions = summary.recentTransactions,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                Box(modifier = Modifier.padding(bottom = spacing.medium))
            }
        }
    }
}

@Composable
private fun Centered(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
