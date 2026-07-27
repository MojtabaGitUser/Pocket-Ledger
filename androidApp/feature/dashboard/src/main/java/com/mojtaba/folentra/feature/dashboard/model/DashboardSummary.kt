package com.mojtaba.folentra.feature.dashboard.model

data class DashboardSummary(
    val period: DashboardPeriod,
    val cashFlow: CashFlowSummary,
    val topCategories: List<CategorySpendSummary>,
    val budgetProgress: List<BudgetProgressSummary>,
    val recentTransactions: List<RecentTransactionSummary>,
    val insights: List<DashboardInsight>,
    val generatedAt: Long,
)
