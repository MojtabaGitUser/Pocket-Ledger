package com.mojtaba.pocketledger.feature.dashboard.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.mojtaba.pocketledger.core.designsystem.component.EmptyState
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressStatus
import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressSummary
import com.mojtaba.pocketledger.feature.dashboard.presentation.model.DashboardFormatters

@Composable
fun BudgetProgressCard(
    budgets: List<BudgetProgressSummary>,
    onSetBudgetClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = "Budgets",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            if (budgets.isEmpty()) {
                EmptyState(
                    title = "No active budgets",
                    message = "Budget progress will appear here.",
                )
                Button(onClick = onSetBudgetClick) {
                    Text("Set budget")
                }
            } else {
                budgets.forEach { budget ->
                    BudgetProgressRow(budget = budget)
                }
                Button(onClick = onSetBudgetClick) {
                    Text("Add budget")
                }
            }
        }
    }
}

@Composable
private fun BudgetProgressRow(
    budget: BudgetProgressSummary,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing
    val spent = DashboardFormatters.formatAmountMinor(budget.spentMinor, budget.currencyCode)
    val limit = DashboardFormatters.formatAmountMinor(budget.limitMinor, budget.currencyCode)
    val percent = DashboardFormatters.percent(budget.progressPercent)
    val status = DashboardFormatters.budgetStatusLabel(budget.status)
    val statusColor = when (budget.status) {
        BudgetProgressStatus.NoLimit,
        BudgetProgressStatus.OnTrack,
        -> MaterialTheme.colorScheme.primary
        BudgetProgressStatus.NearLimit -> MaterialTheme.colorScheme.tertiary
        BudgetProgressStatus.Exceeded -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${budget.budgetName}, $spent spent of $limit, $percent, $status"
            },
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
            ) {
                Text(
                    text = budget.budgetName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = listOfNotNull(budget.categoryName, "$spent of $limit")
                        .joinToString(separator = " - "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(status) },
            )
        }
        LinearProgressIndicator(
            progress = {
                if (budget.limitMinor <= 0L) 0f else (budget.progressPercent / 100.0)
                    .coerceIn(0.0, 1.0)
                    .toFloat()
            },
            color = statusColor,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "$percent budget progress" },
        )
    }
}
