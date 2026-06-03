package com.mojtaba.pocketledger.feature.dashboard.presentation.preview

import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressStatus
import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressSummary
import com.mojtaba.pocketledger.feature.dashboard.model.CashFlowSummary
import com.mojtaba.pocketledger.feature.dashboard.model.CategorySpendSummary
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardInsight
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardPeriod
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardSummary
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardTransactionType
import com.mojtaba.pocketledger.feature.dashboard.model.RecentTransactionSummary

object DashboardPreviewFixtures {
    val summary = DashboardSummary(
        period = DashboardPeriod(
            startMillis = 1_700_000_000_000L,
            endMillis = 1_702_591_999_999L,
            label = "November 2023",
        ),
        cashFlow = CashFlowSummary(
            incomeMinor = 4_850_00,
            expenseMinor = 2_318_45,
            netMinor = 2_531_55,
            currencyCode = "USD",
        ),
        topCategories = listOf(
            CategorySpendSummary(
                categoryId = "housing",
                categoryName = "Housing",
                amountMinor = 1_250_00,
                currencyCode = "USD",
                transactionCount = 1,
                percentageOfExpense = 53.9,
            ),
            CategorySpendSummary(
                categoryId = "food",
                categoryName = "Food and dining",
                amountMinor = 524_20,
                currencyCode = "USD",
                transactionCount = 12,
                percentageOfExpense = 22.6,
            ),
            CategorySpendSummary(
                categoryId = "transport",
                categoryName = "Transportation",
                amountMinor = 276_15,
                currencyCode = "USD",
                transactionCount = 6,
                percentageOfExpense = 11.9,
            ),
        ),
        budgetProgress = listOf(
            BudgetProgressSummary(
                budgetId = "food-budget",
                budgetName = "Food budget",
                categoryId = "food",
                categoryName = "Food and dining",
                spentMinor = 524_20,
                limitMinor = 650_00,
                currencyCode = "USD",
                progressPercent = 80.6,
                status = BudgetProgressStatus.NearLimit,
            ),
            BudgetProgressSummary(
                budgetId = "transport-budget",
                budgetName = "Transport budget",
                categoryId = "transport",
                categoryName = "Transportation",
                spentMinor = 276_15,
                limitMinor = 300_00,
                currencyCode = "USD",
                progressPercent = 92.1,
                status = BudgetProgressStatus.NearLimit,
            ),
        ),
        recentTransactions = listOf(
            RecentTransactionSummary(
                transactionId = "paycheck",
                amountMinor = 2_400_00,
                currencyCode = "USD",
                type = DashboardTransactionType.Income,
                categoryName = "Salary",
                notePreview = "Monthly paycheck",
                occurredAt = 1_700_940_000_000L,
            ),
            RecentTransactionSummary(
                transactionId = "groceries",
                amountMinor = -86_32,
                currencyCode = "USD",
                type = DashboardTransactionType.Expense,
                categoryName = "Food and dining",
                notePreview = "Groceries",
                occurredAt = 1_700_850_000_000L,
            ),
            RecentTransactionSummary(
                transactionId = "rent",
                amountMinor = -1_250_00,
                currencyCode = "USD",
                type = DashboardTransactionType.Expense,
                categoryName = "Housing",
                notePreview = null,
                occurredAt = 1_700_100_000_000L,
            ),
        ),
        insights = listOf(
            DashboardInsight.PositiveCashFlow(
                netMinor = 2_531_55,
                currencyCode = "USD",
            ),
            DashboardInsight.OverspendingCategory(
                categoryId = "housing",
                categoryName = "Housing",
                amountMinor = 1_250_00,
                currencyCode = "USD",
                percentageOfExpense = 53.9,
            ),
            DashboardInsight.BudgetNearLimit(
                budgetId = "food-budget",
                budgetName = "Food budget",
                progressPercent = 80.6,
            ),
        ),
        generatedAt = 1_700_950_000_000L,
    )

    val emptySummary = summary.copy(
        cashFlow = summary.cashFlow.copy(
            incomeMinor = 0L,
            expenseMinor = 0L,
            netMinor = 0L,
        ),
        topCategories = emptyList(),
        budgetProgress = emptyList(),
        recentTransactions = emptyList(),
        insights = listOf(DashboardInsight.NoData),
    )
}
