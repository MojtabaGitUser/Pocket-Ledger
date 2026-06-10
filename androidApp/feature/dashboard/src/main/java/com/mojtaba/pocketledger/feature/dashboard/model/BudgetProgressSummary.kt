package com.mojtaba.pocketledger.feature.dashboard.model

data class BudgetProgressSummary(
    val budgetId: String,
    val budgetName: String,
    val categoryId: String?,
    val categoryName: String?,
    val spentMinor: Long,
    val limitMinor: Long,
    val currencyCode: String,
    val progressPercent: Double,
    val status: BudgetProgressStatus,
)
