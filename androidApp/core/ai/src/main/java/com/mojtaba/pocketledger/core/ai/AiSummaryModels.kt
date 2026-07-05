package com.mojtaba.pocketledger.core.ai

data class AiSummaryRequest(
    val periodLabel: String,
    val facts: List<String>,
    val maxSentences: Int = 3,
)

data class AiSummaryResult(
    val text: String,
)

data class MonthlySummaryRequest(
    val periodLabel: String,
    val startMillis: Long,
    val endMillis: Long,
    val currencyCode: String,
    val totalIncomeMinor: Long,
    val totalExpenseMinor: Long,
    val transactionCount: Int,
    val categorySummaries: List<AiCategorySummary> = emptyList(),
    val recurringHints: List<AiRecurringHint> = emptyList(),
    val budgetComparisons: List<AiBudgetComparison> = emptyList(),
    val privacyMode: AiPrivacyMode = AiPrivacyMode.AggregateOnly,
)

data class AiCategorySummary(
    val categoryId: String?,
    val displayName: String?,
    val totalExpenseMinor: Long,
    val transactionCount: Int,
)

data class AiRecurringHint(
    val label: String,
    val transactionCount: Int,
)

data class AiBudgetComparison(
    val budgetId: String,
    val displayName: String,
    val spentMinor: Long,
    val budgetMinor: Long,
)

data class MonthlySummaryResult(
    val title: String,
    val summaryText: String,
    val insights: List<String>,
    val warnings: List<String> = emptyList(),
    val suggestedActions: List<String> = emptyList(),
    val providerType: AiProviderType,
    val quality: AiResultQuality = AiResultQuality.Medium,
    val fallbackReason: String? = null,
    val privacyMode: AiPrivacyMode = AiPrivacyMode.AggregateOnly,
)

enum class AiPrivacyMode {
    AggregateOnly,
    LocalRawAllowed,
}

enum class AiResultQuality {
    Low,
    Medium,
    High,
}
