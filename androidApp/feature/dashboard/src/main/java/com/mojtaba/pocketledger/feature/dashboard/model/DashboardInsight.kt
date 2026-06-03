package com.mojtaba.pocketledger.feature.dashboard.model

sealed interface DashboardInsight {
    data object NoData : DashboardInsight

    data class PositiveCashFlow(
        val netMinor: Long,
        val currencyCode: String,
    ) : DashboardInsight

    data class NegativeCashFlow(
        val netMinor: Long,
        val currencyCode: String,
    ) : DashboardInsight

    data class OverspendingCategory(
        val categoryId: String?,
        val categoryName: String,
        val amountMinor: Long,
        val currencyCode: String,
        val percentageOfExpense: Double,
    ) : DashboardInsight

    data class BudgetNearLimit(
        val budgetId: String,
        val budgetName: String,
        val progressPercent: Double,
    ) : DashboardInsight

    data class BudgetExceeded(
        val budgetId: String,
        val budgetName: String,
        val progressPercent: Double,
    ) : DashboardInsight
}
