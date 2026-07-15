package com.mojtaba.pocketledger.feature.dashboard.domain

import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressStatus
import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressSummary
import com.mojtaba.pocketledger.feature.dashboard.model.CashFlowSummary
import com.mojtaba.pocketledger.feature.dashboard.model.CategorySpendSummary
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardInsight
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardPeriod
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardSummary
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardTransactionType
import com.mojtaba.pocketledger.feature.dashboard.model.RecentTransactionSummary
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedBudgetProgressStatus
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedBudgetProgressSummary
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedCashFlowSummary
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedCategorySpendSummary
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedDashboardBudget
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedDashboardCategory
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedDashboardInput
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedDashboardInsight
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedDashboardPeriod
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedDashboardSummary
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedDashboardSummaryCalculator
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedDashboardTransaction
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedDashboardTransactionType
import com.mojtaba.pocketledger.shared.domain.dashboard.SharedRecentTransactionSummary

object DashboardSummaryCalculator {
    const val DefaultRecentTransactionLimit = SharedDashboardSummaryCalculator.DefaultRecentTransactionLimit
    const val DefaultTopCategoryLimit = SharedDashboardSummaryCalculator.DefaultTopCategoryLimit

    fun calculate(input: DashboardSummaryInput): DashboardSummary =
        SharedDashboardSummaryCalculator.calculate(input.toSharedInput()).toFeatureSummary()

    private fun DashboardSummaryInput.toSharedInput(): SharedDashboardInput = SharedDashboardInput(
        period = period.toSharedPeriod(),
        currencyCode = currencyCode,
        transactions = transactions.map { it.toSharedTransaction() },
        categories = categories.map { it.toSharedCategory() },
        budgets = budgets.map { it.toSharedBudget() },
        generatedAt = generatedAt,
        recentTransactionLimit = recentTransactionLimit,
        topCategoryLimit = topCategoryLimit,
    )

    private fun DashboardPeriod.toSharedPeriod(): SharedDashboardPeriod = SharedDashboardPeriod(
        startMillis = startMillis,
        endMillis = endMillis,
        label = label,
    )

    private fun LedgerTransaction.toSharedTransaction(): SharedDashboardTransaction = SharedDashboardTransaction(
        id = id,
        amountMinor = amountMinor,
        type = type,
        categoryId = categoryId,
        note = note,
        occurredAt = occurredAt,
        currencyCode = currencyCode,
    )

    private fun LedgerCategory.toSharedCategory(): SharedDashboardCategory = SharedDashboardCategory(
        id = id,
        name = name,
    )

    private fun LedgerBudget.toSharedBudget(): SharedDashboardBudget = SharedDashboardBudget(
        id = id,
        name = name,
        amountMinor = amountMinor,
        currencyCode = currencyCode,
        periodStart = periodStart,
        periodEnd = periodEnd,
        categoryId = categoryId,
        isActive = isActive,
    )

    private fun SharedDashboardSummary.toFeatureSummary(): DashboardSummary = DashboardSummary(
        period = period.toFeaturePeriod(),
        cashFlow = cashFlow.toFeatureCashFlow(),
        topCategories = topCategories.map { it.toFeatureCategorySpend() },
        budgetProgress = budgetProgress.map { it.toFeatureBudgetProgress() },
        recentTransactions = recentTransactions.map { it.toFeatureRecentTransaction() },
        insights = insights.map { it.toFeatureInsight() },
        generatedAt = generatedAt,
    )

    private fun SharedDashboardPeriod.toFeaturePeriod(): DashboardPeriod = DashboardPeriod(
        startMillis = startMillis,
        endMillis = endMillis,
        label = label,
    )

    private fun SharedCashFlowSummary.toFeatureCashFlow(): CashFlowSummary = CashFlowSummary(
        incomeMinor = incomeMinor,
        expenseMinor = expenseMinor,
        netMinor = netMinor,
        currencyCode = currencyCode,
    )

    private fun SharedCategorySpendSummary.toFeatureCategorySpend(): CategorySpendSummary = CategorySpendSummary(
        categoryId = categoryId,
        categoryName = categoryName,
        amountMinor = amountMinor,
        currencyCode = currencyCode,
        transactionCount = transactionCount,
        percentageOfExpense = percentageOfExpense,
    )

    private fun SharedBudgetProgressSummary.toFeatureBudgetProgress(): BudgetProgressSummary = BudgetProgressSummary(
        budgetId = budgetId,
        budgetName = budgetName,
        categoryId = categoryId,
        categoryName = categoryName,
        spentMinor = spentMinor,
        limitMinor = limitMinor,
        currencyCode = currencyCode,
        progressPercent = progressPercent,
        status = status.toFeatureStatus(),
    )

    private fun SharedBudgetProgressStatus.toFeatureStatus(): BudgetProgressStatus = when (this) {
        SharedBudgetProgressStatus.NoLimit -> BudgetProgressStatus.NoLimit
        SharedBudgetProgressStatus.OnTrack -> BudgetProgressStatus.OnTrack
        SharedBudgetProgressStatus.NearLimit -> BudgetProgressStatus.NearLimit
        SharedBudgetProgressStatus.Exceeded -> BudgetProgressStatus.Exceeded
    }

    private fun SharedRecentTransactionSummary.toFeatureRecentTransaction(): RecentTransactionSummary = RecentTransactionSummary(
        transactionId = transactionId,
        amountMinor = amountMinor,
        currencyCode = currencyCode,
        type = type.toFeatureType(),
        categoryName = categoryName,
        notePreview = notePreview,
        occurredAt = occurredAt,
    )

    private fun SharedDashboardTransactionType.toFeatureType(): DashboardTransactionType = when (this) {
        SharedDashboardTransactionType.Income -> DashboardTransactionType.Income
        SharedDashboardTransactionType.Expense -> DashboardTransactionType.Expense
        SharedDashboardTransactionType.Unknown -> DashboardTransactionType.Unknown
    }

    private fun SharedDashboardInsight.toFeatureInsight(): DashboardInsight = when (this) {
        SharedDashboardInsight.NoData -> DashboardInsight.NoData
        is SharedDashboardInsight.PositiveCashFlow -> DashboardInsight.PositiveCashFlow(
            netMinor = netMinor,
            currencyCode = currencyCode,
        )
        is SharedDashboardInsight.NegativeCashFlow -> DashboardInsight.NegativeCashFlow(
            netMinor = netMinor,
            currencyCode = currencyCode,
        )
        is SharedDashboardInsight.OverspendingCategory -> DashboardInsight.OverspendingCategory(
            categoryId = categoryId,
            categoryName = categoryName,
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            percentageOfExpense = percentageOfExpense,
        )
        is SharedDashboardInsight.BudgetNearLimit -> DashboardInsight.BudgetNearLimit(
            budgetId = budgetId,
            budgetName = budgetName,
            progressPercent = progressPercent,
        )
        is SharedDashboardInsight.BudgetExceeded -> DashboardInsight.BudgetExceeded(
            budgetId = budgetId,
            budgetName = budgetName,
            progressPercent = progressPercent,
        )
    }
}
