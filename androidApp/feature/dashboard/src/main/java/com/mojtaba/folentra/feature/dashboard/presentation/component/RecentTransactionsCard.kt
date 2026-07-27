package com.mojtaba.folentra.feature.dashboard.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.mojtaba.folentra.core.designsystem.component.AmountDisplay
import com.mojtaba.folentra.core.designsystem.component.AmountTone
import com.mojtaba.folentra.core.designsystem.component.EmptyState
import com.mojtaba.folentra.core.designsystem.component.TransactionRow
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults
import com.mojtaba.folentra.feature.dashboard.model.DashboardTransactionType
import com.mojtaba.folentra.feature.dashboard.model.RecentTransactionSummary
import com.mojtaba.folentra.feature.dashboard.presentation.model.DashboardFormatters

@Composable
fun RecentTransactionsCard(
    transactions: List<RecentTransactionSummary>,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    val visibleTransactions = remember(transactions) {
        transactions.take(5).map { transaction -> transaction.toDisplayRow() }
    }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = "Recent transactions",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            if (transactions.isEmpty()) {
                EmptyState(
                    title = "No recent transactions",
                    message = "Recent ledger activity will appear here.",
                )
            } else {
                visibleTransactions.forEachIndexed { index, transaction ->
                    TransactionRow(
                        title = transaction.title,
                        amount = transaction.amount,
                        category = transaction.category,
                        subtitle = transaction.subtitle,
                        showDivider = index < visibleTransactions.lastIndex,
                    )
                }
            }
        }
    }
}

@Immutable
private data class RecentTransactionDisplayRow(
    val title: String,
    val amount: AmountDisplay,
    val category: String,
    val subtitle: String?,
)

private fun RecentTransactionSummary.toDisplayRow(): RecentTransactionDisplayRow {
    val typeLabel = DashboardFormatters.transactionTypeLabel(type)
    return RecentTransactionDisplayRow(
        title = categoryName ?: typeLabel,
        amount = DashboardFormatters.amount(
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            includeSign = true,
            tone = when (type) {
                DashboardTransactionType.Income -> AmountTone.Positive
                DashboardTransactionType.Expense -> AmountTone.Negative
                DashboardTransactionType.Unknown -> AmountTone.Neutral
            },
        ),
        category = typeLabel,
        subtitle = listOfNotNull(
            DashboardFormatters.date(occurredAt),
            notePreview,
        ).joinToString(separator = " - ").takeIf { it.isNotBlank() },
    )
}
