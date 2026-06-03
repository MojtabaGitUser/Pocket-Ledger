package com.mojtaba.pocketledger.feature.dashboard.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.mojtaba.pocketledger.core.designsystem.component.AmountTone
import com.mojtaba.pocketledger.core.designsystem.component.EmptyState
import com.mojtaba.pocketledger.core.designsystem.component.TransactionRow
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardTransactionType
import com.mojtaba.pocketledger.feature.dashboard.model.RecentTransactionSummary
import com.mojtaba.pocketledger.feature.dashboard.presentation.model.DashboardFormatters

@Composable
fun RecentTransactionsCard(
    transactions: List<RecentTransactionSummary>,
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
                transactions.take(5).forEachIndexed { index, transaction ->
                    TransactionRow(
                        title = transaction.categoryName ?: DashboardFormatters.transactionTypeLabel(transaction.type),
                        amount = DashboardFormatters.amount(
                            amountMinor = transaction.amountMinor,
                            currencyCode = transaction.currencyCode,
                            includeSign = true,
                            tone = when (transaction.type) {
                                DashboardTransactionType.Income -> AmountTone.Positive
                                DashboardTransactionType.Expense -> AmountTone.Negative
                                DashboardTransactionType.Unknown -> AmountTone.Neutral
                            },
                        ),
                        category = DashboardFormatters.transactionTypeLabel(transaction.type),
                        subtitle = listOfNotNull(
                            DashboardFormatters.date(transaction.occurredAt),
                            transaction.notePreview,
                        ).joinToString(separator = " - ").takeIf { it.isNotBlank() },
                        showDivider = index < transactions.take(5).lastIndex,
                    )
                }
            }
        }
    }
}
