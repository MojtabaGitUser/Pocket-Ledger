package com.mojtaba.folentra.feature.dashboard.domain

import com.mojtaba.folentra.core.data.model.LedgerBudget
import com.mojtaba.folentra.core.data.model.LedgerCategory
import com.mojtaba.folentra.core.data.model.LedgerTransaction
import com.mojtaba.folentra.feature.dashboard.model.DashboardPeriod

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
