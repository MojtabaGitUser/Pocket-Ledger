package com.mojtaba.folentra.shared.domain.dashboard

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedDashboardSummaryCalculatorTest {
    @Test
    fun calculatesCashFlowTopCategoriesAndBudgetStatus() {
        val summary = SharedDashboardSummaryCalculator.calculate(
            SharedDashboardInput(
                transactions = listOf(
                    SharedDashboardTransaction("income", 200_00, "income", null, null, 10L, "usd"),
                    SharedDashboardTransaction("rent", -150_00, "expense", "housing", "Monthly rent", 20L, "USD"),
                    SharedDashboardTransaction("food", -25_00, "expense", "food", "Market run", 30L, "USD"),
                ),
                categories = listOf(
                    SharedDashboardCategory("housing", "Housing"),
                    SharedDashboardCategory("food", "Food"),
                ),
                budgets = listOf(
                    SharedDashboardBudget("budget", "Housing budget", 100_00, "USD", 0L, 100L, "housing", true),
                ),
                period = SharedDashboardPeriod(0L, 100L, "Test"),
                currencyCode = "USD",
                generatedAt = 50L,
            ),
        )

        assertEquals(200_00, summary.cashFlow.incomeMinor)
        assertEquals(175_00, summary.cashFlow.expenseMinor)
        assertEquals("Housing", summary.topCategories.first().categoryName)
        assertEquals(SharedBudgetProgressStatus.Exceeded, summary.budgetProgress.single().status)
        assertTrue(summary.insights.any { it is SharedDashboardInsight.BudgetExceeded })
    }
}