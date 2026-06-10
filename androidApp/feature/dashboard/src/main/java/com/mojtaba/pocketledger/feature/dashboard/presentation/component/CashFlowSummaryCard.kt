package com.mojtaba.pocketledger.feature.dashboard.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mojtaba.pocketledger.core.designsystem.component.AmountTone
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.dashboard.model.CashFlowSummary
import com.mojtaba.pocketledger.feature.dashboard.presentation.model.DashboardFormatters

@Composable
fun CashFlowSummaryCard(
    cashFlow: CashFlowSummary,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Text(
            text = "Cash flow",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val wide = maxWidth >= 620.dp
            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    CashFlowMetricCards(cashFlow = cashFlow, itemModifier = Modifier.weight(1f))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    CashFlowMetricCards(cashFlow = cashFlow, itemModifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun CashFlowMetricCards(
    cashFlow: CashFlowSummary,
    itemModifier: Modifier,
) {
    DashboardMetricCard(
        label = "Income",
        amount = DashboardFormatters.amount(
            amountMinor = cashFlow.incomeMinor,
            currencyCode = cashFlow.currencyCode,
            tone = AmountTone.Positive,
        ),
        supportingText = "Money in",
        modifier = itemModifier,
    )
    DashboardMetricCard(
        label = "Expenses",
        amount = DashboardFormatters.amount(
            amountMinor = cashFlow.expenseMinor,
            currencyCode = cashFlow.currencyCode,
            tone = AmountTone.Negative,
        ),
        supportingText = "Money out",
        modifier = itemModifier,
    )
    DashboardMetricCard(
        label = "Net",
        amount = DashboardFormatters.amount(
            amountMinor = cashFlow.netMinor,
            currencyCode = cashFlow.currencyCode,
            includeSign = true,
            tone = when {
                cashFlow.netMinor > 0L -> AmountTone.Positive
                cashFlow.netMinor < 0L -> AmountTone.Negative
                else -> AmountTone.Neutral
            },
        ),
        supportingText = "Income minus expenses",
        modifier = itemModifier,
    )
}
