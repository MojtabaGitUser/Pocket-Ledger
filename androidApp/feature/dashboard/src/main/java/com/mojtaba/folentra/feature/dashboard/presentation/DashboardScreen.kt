package com.mojtaba.folentra.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mojtaba.folentra.core.designsystem.accessibility.folentraHeading
import com.mojtaba.folentra.core.designsystem.adaptive.FolentraWindowWidthSizeClass
import com.mojtaba.folentra.core.designsystem.component.AdaptiveContainer
import com.mojtaba.folentra.core.designsystem.component.ErrorState
import com.mojtaba.folentra.core.designsystem.component.LoadingState
import com.mojtaba.folentra.core.designsystem.component.SectionHeader
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults
import com.mojtaba.folentra.feature.dashboard.model.DashboardSummary
import com.mojtaba.folentra.feature.dashboard.presentation.component.BudgetProgressCard
import com.mojtaba.folentra.feature.dashboard.presentation.component.CashFlowSummaryCard
import com.mojtaba.folentra.feature.dashboard.presentation.component.CategorySpendChart
import com.mojtaba.folentra.feature.dashboard.presentation.component.DashboardEmptyState
import com.mojtaba.folentra.feature.dashboard.presentation.component.DashboardInsightCard
import com.mojtaba.folentra.feature.dashboard.presentation.component.RecentTransactionsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
    widthSizeClass: FolentraWindowWidthSizeClass = FolentraWindowWidthSizeClass.Compact,
) {
    val fontScale = LocalDensity.current.fontScale
    val horizontalPadding = when (widthSizeClass) {
        FolentraWindowWidthSizeClass.Compact -> 16.dp
        FolentraWindowWidthSizeClass.Medium -> 24.dp
        FolentraWindowWidthSizeClass.Expanded -> 32.dp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
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
            maxWidth = 1180.dp,
            horizontalPadding = horizontalPadding,
        ) {
            DashboardContent(
                uiState = uiState,
                layoutMode = dashboardLayoutMode(widthSizeClass, fontScale),
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    layoutMode: DashboardLayoutMode,
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
            layoutMode = layoutMode,
            onSetBudgetClick = { onAction(DashboardAction.SetBudgetClicked) },
            modifier = modifier,
        )
    }
}

@Composable
private fun DashboardSummaryContent(
    summary: DashboardSummary,
    layoutMode: DashboardLayoutMode,
    onSetBudgetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("DashboardContentList"),
        contentPadding = PaddingValues(vertical = spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        item {
            SectionHeader(
                title = "Financial overview",
                subtitle = summary.period.label,
                modifier = Modifier
                    .semantics { heading() },
            )
        }
        item {
            CashFlowSummaryCard(
                cashFlow = summary.cashFlow,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        when (layoutMode) {
            DashboardLayoutMode.SingleColumn -> singleColumnDashboardItems(
                summary = summary,
                onSetBudgetClick = onSetBudgetClick,
            )
            DashboardLayoutMode.TwoColumn -> twoColumnDashboardItems(
                summary = summary,
                onSetBudgetClick = onSetBudgetClick,
            )
            DashboardLayoutMode.DashboardGrid -> dashboardGridItems(
                summary = summary,
                onSetBudgetClick = onSetBudgetClick,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.singleColumnDashboardItems(
    summary: DashboardSummary,
    onSetBudgetClick: () -> Unit,
) {
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

private fun androidx.compose.foundation.lazy.LazyListScope.twoColumnDashboardItems(
    summary: DashboardSummary,
    onSetBudgetClick: () -> Unit,
) {
    item {
        DashboardTwoColumnRow {
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
        DashboardTwoColumnRow {
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
}

private fun androidx.compose.foundation.lazy.LazyListScope.dashboardGridItems(
    summary: DashboardSummary,
    onSetBudgetClick: () -> Unit,
) {
    item {
        DashboardThreeColumnRow {
            CategorySpendChart(
                categories = summary.topCategories,
                modifier = Modifier.weight(1f),
            )
            BudgetProgressCard(
                budgets = summary.budgetProgress,
                onSetBudgetClick = onSetBudgetClick,
                modifier = Modifier.weight(1f),
            )
            DashboardInsightCard(
                insights = summary.insights,
                modifier = Modifier.weight(1f),
            )
        }
    }
    item {
        RecentTransactionsCard(
            transactions = summary.recentTransactions,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DashboardTwoColumnRow(
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.medium),
        content = content,
    )
}

@Composable
private fun DashboardThreeColumnRow(
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.medium),
        content = content,
    )
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
