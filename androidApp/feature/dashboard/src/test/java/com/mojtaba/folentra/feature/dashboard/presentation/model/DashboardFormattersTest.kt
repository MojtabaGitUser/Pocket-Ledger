package com.mojtaba.folentra.feature.dashboard.presentation.model

import com.mojtaba.folentra.feature.dashboard.model.BudgetProgressStatus
import com.mojtaba.folentra.feature.dashboard.model.DashboardInsight
import com.mojtaba.folentra.feature.dashboard.model.DashboardTransactionType
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardFormattersTest {
    @Test
    fun amountFormattingUsesMinorUnits() {
        assertEquals("\$12.34", DashboardFormatters.formatAmountMinor(1_234, "USD"))
        assertEquals("-\$12.34", DashboardFormatters.formatAmountMinor(-1_234, "USD", includeSign = true))
        assertEquals("+\$12.34", DashboardFormatters.formatAmountMinor(1_234, "USD", includeSign = true))
        assertEquals("\$0.00", DashboardFormatters.formatAmountMinor(0, "USD", includeSign = true))
        assertEquals("\$12.34", DashboardFormatters.formatAmountMinor(1_234, "not-a-currency"))
    }

    @Test
    fun percentFormattingRoundsToWholePercent() {
        assertEquals("81%", DashboardFormatters.percent(80.6))
        assertEquals("0%", DashboardFormatters.percent(0.0))
    }

    @Test
    fun dateFormattingUsesProvidedZone() {
        assertEquals(
            "Nov 14",
            DashboardFormatters.date(1_700_000_000_000L, ZoneOffset.UTC),
        )
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
        assertEquals("No data yet", DashboardFormatters.insightTitle(DashboardInsight.NoData))
        assertEquals(
            "Add transactions to start seeing dashboard insights.",
            DashboardFormatters.insightMessage(DashboardInsight.NoData),
        )
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
        assertEquals("Negative cash flow", DashboardFormatters.insightTitle(DashboardInsight.NegativeCashFlow(-500, "USD")))
        assertEquals(
            "Net cash flow is -\$5.00.",
            DashboardFormatters.insightMessage(
                DashboardInsight.NegativeCashFlow(
                    netMinor = -500,
                    currencyCode = "USD",
                ),
            ),
        )
        assertEquals("Budget near limit", DashboardFormatters.insightTitle(DashboardInsight.BudgetNearLimit("b1", "Food", 80.4)))
        assertEquals(
            "Food is at 80% of its limit.",
            DashboardFormatters.insightMessage(
                DashboardInsight.BudgetNearLimit(
                    budgetId = "b1",
                    budgetName = "Food",
                    progressPercent = 80.4,
                ),
            ),
        )
        assertEquals("Budget exceeded", DashboardFormatters.insightTitle(DashboardInsight.BudgetExceeded("b2", "Rent", 125.1)))
        assertEquals(
            "Rent is over budget at 125%.",
            DashboardFormatters.insightMessage(
                DashboardInsight.BudgetExceeded(
                    budgetId = "b2",
                    budgetName = "Rent",
                    progressPercent = 125.1,
                ),
            ),
        )
    }
}
