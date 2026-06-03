package com.mojtaba.pocketledger.feature.dashboard.presentation.model

import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressStatus
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardInsight
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardTransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardFormattersTest {
    @Test
    fun amountFormattingUsesMinorUnits() {
        assertEquals("\$12.34", DashboardFormatters.formatAmountMinor(1_234, "USD"))
        assertEquals("-\$12.34", DashboardFormatters.formatAmountMinor(-1_234, "USD", includeSign = true))
        assertEquals("+\$12.34", DashboardFormatters.formatAmountMinor(1_234, "USD", includeSign = true))
    }

    @Test
    fun percentFormattingRoundsToWholePercent() {
        assertEquals("81%", DashboardFormatters.percent(80.6))
        assertEquals("0%", DashboardFormatters.percent(0.0))
    }

    @Test
    fun budgetStatusLabelsAreStable() {
        assertEquals("No limit", DashboardFormatters.budgetStatusLabel(BudgetProgressStatus.NoLimit))
        assertEquals("On track", DashboardFormatters.budgetStatusLabel(BudgetProgressStatus.OnTrack))
        assertEquals("Near limit", DashboardFormatters.budgetStatusLabel(BudgetProgressStatus.NearLimit))
        assertEquals("Exceeded", DashboardFormatters.budgetStatusLabel(BudgetProgressStatus.Exceeded))
    }

    @Test
    fun transactionTypeLabelsAreStable() {
        assertEquals("Income", DashboardFormatters.transactionTypeLabel(DashboardTransactionType.Income))
        assertEquals("Expense", DashboardFormatters.transactionTypeLabel(DashboardTransactionType.Expense))
        assertEquals("Unknown", DashboardFormatters.transactionTypeLabel(DashboardTransactionType.Unknown))
    }

    @Test
    fun insightMessagesAreDeterministic() {
        assertEquals(
            "Net cash flow is +\$10.00.",
            DashboardFormatters.insightMessage(
                DashboardInsight.PositiveCashFlow(
                    netMinor = 1_000,
                    currencyCode = "USD",
                ),
            ),
        )
        assertEquals(
            "Food accounts for 54% of expenses.",
            DashboardFormatters.insightMessage(
                DashboardInsight.OverspendingCategory(
                    categoryId = "food",
                    categoryName = "Food",
                    amountMinor = 5_000,
                    currencyCode = "USD",
                    percentageOfExpense = 53.9,
                ),
            ),
        )
    }
}
