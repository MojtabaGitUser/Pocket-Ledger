package com.mojtaba.pocketledger.feature.dashboard.model

data class CashFlowSummary(
    val incomeMinor: Long,
    val expenseMinor: Long,
    val netMinor: Long,
    val currencyCode: String,
)
