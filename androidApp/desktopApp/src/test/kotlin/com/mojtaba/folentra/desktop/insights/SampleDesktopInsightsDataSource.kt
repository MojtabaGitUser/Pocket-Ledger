package com.mojtaba.folentra.desktop.insights

object SampleDesktopInsightsDataSource {
    fun monthlySummary(): DesktopMonthlySummaryInput =
        DesktopMonthlySummaryInput(
            periodLabel = "February 2026",
            currencyCode = "USD",
            totalIncomeMinor = 368_000,
            totalExpenseMinor = 256_885,
            transactionCount = 16,
            categories = listOf(
                DesktopCategorySummary("Housing", 145_000, 1),
                DesktopCategorySummary("Groceries", 22_165, 3),
                DesktopCategorySummary("Utilities", 20_740, 2),
                DesktopCategorySummary("Transportation", 13_820, 2),
                DesktopCategorySummary("Dining", 8_460, 3),
                DesktopCategorySummary("Entertainment", 11_700, 2),
                DesktopCategorySummary("Savings", 35_000, 1),
            ),
            budgets = listOf(
                DesktopBudgetComparison("Groceries sample budget", 22_165, 55_000),
                DesktopBudgetComparison("Dining sample budget", 8_460, 28_000),
                DesktopBudgetComparison("Transportation sample budget", 13_820, 18_000),
                DesktopBudgetComparison("Entertainment sample budget", 11_700, 16_000),
            ),
            recurringHints = listOf(
                DesktopRecurringHint("Recurring sample bills", 4),
                DesktopRecurringHint("Groceries", 3),
            ),
        )
}
