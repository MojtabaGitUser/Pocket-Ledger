package com.mojtaba.pocketledger.feature.dashboard.model

data class CategorySpendSummary(
    val categoryId: String?,
    val categoryName: String,
    val amountMinor: Long,
    val currencyCode: String,
    val transactionCount: Int,
    val percentageOfExpense: Double,
)
