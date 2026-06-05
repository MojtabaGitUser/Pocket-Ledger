package com.mojtaba.pocketledger.feature.dashboard.budget

data class ValidatedBudgetInput(
    val id: String?,
    val name: String,
    val amountMinor: Long,
    val currencyCode: String,
    val periodType: String = BudgetSetupState.MONTHLY_PERIOD_TYPE,
    val periodStart: Long,
    val periodEnd: Long,
    val categoryId: String?,
    val isActive: Boolean,
)
