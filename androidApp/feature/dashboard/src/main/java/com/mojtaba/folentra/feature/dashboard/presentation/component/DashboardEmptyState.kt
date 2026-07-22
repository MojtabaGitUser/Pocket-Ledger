package com.mojtaba.folentra.feature.dashboard.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.folentra.core.designsystem.component.EmptyState
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults

@Composable
fun DashboardEmptyState(
    onSetBudgetClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.medium),
    ) {
        EmptyState(
            title = "No dashboard data yet",
            message = "Add transactions and budgets to see cash flow, spending, and insights.",
        )
        Button(onClick = onSetBudgetClick) {
            Text("Set budget")
        }
    }
}
