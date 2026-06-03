package com.mojtaba.pocketledger.feature.dashboard.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.pocketledger.core.designsystem.component.EmptyState

@Composable
fun DashboardEmptyState(
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = "No dashboard data yet",
        message = "Add transactions and budgets to see cash flow, spending, and insights.",
        modifier = modifier,
    )
}
