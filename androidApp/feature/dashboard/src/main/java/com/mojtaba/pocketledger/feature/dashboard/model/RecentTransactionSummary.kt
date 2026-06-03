package com.mojtaba.pocketledger.feature.dashboard.model

data class RecentTransactionSummary(
    val transactionId: String,
    val amountMinor: Long,
    val currencyCode: String,
    val type: DashboardTransactionType,
    val categoryName: String?,
    val notePreview: String?,
    val occurredAt: Long,
)
