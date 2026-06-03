package com.mojtaba.pocketledger.feature.dashboard.domain

import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardPeriod

data class DashboardSummaryInput(
    val period: DashboardPeriod,
    val currencyCode: String,
    val transactions: List<LedgerTransaction> = emptyList(),
    val categories: List<LedgerCategory> = emptyList(),
    val budgets: List<LedgerBudget> = emptyList(),
    val generatedAt: Long,
    val recentTransactionLimit: Int = DashboardSummaryCalculator.DefaultRecentTransactionLimit,
    val topCategoryLimit: Int = DashboardSummaryCalculator.DefaultTopCategoryLimit,
)
