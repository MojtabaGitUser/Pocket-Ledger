package com.mojtaba.pocketledger.desktop.insights

data class DesktopMonthlySummaryInput(
    val periodLabel: String,
    val currencyCode: String,
    val totalIncomeMinor: Long,
    val totalExpenseMinor: Long,
    val transactionCount: Int,
    val categories: List<DesktopCategorySummary>,
    val budgets: List<DesktopBudgetComparison>,
    val recurringHints: List<DesktopRecurringHint>,
)

data class DesktopCategorySummary(
    val label: String,
    val totalExpenseMinor: Long,
    val transactionCount: Int,
)

data class DesktopBudgetComparison(
    val label: String,
    val spentMinor: Long,
    val budgetMinor: Long,
)

data class DesktopRecurringHint(
    val label: String,
    val transactionCount: Int,
)

data class DesktopMonthlyInsightResult(
    val title: String,
    val summaryText: String,
    val insights: List<String>,
    val warnings: List<String>,
    val suggestedActions: List<String>,
    val providerStatus: DesktopProviderStatus,
)

enum class DesktopProviderStatus {
    RuleBasedFallback,
    LocalOnDevice,
    Unavailable,
}

sealed interface DesktopInsightsUiState {
    data object Loading : DesktopInsightsUiState
    data class Empty(val periodLabel: String) : DesktopInsightsUiState
    data class Error(val message: String) : DesktopInsightsUiState
    data class Content(
        val periodLabel: String,
        val incomeText: String,
        val expenseText: String,
        val netText: String,
        val topCategories: List<DesktopCategorySummary>,
        val result: DesktopMonthlyInsightResult,
    ) : DesktopInsightsUiState
}
