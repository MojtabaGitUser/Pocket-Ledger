package com.mojtaba.folentra.feature.dashboard.budget

data class BudgetSetupState(
    val mode: BudgetSetupMode = BudgetSetupMode.CREATE,
    val budgetId: String? = null,
    val nameInput: String = "",
    val amountInput: String = "",
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
    val categoryId: String? = null,
    val periodStart: Long? = null,
    val periodEnd: Long? = null,
    val isActive: Boolean = true,
) {
    companion object {
        const val DEFAULT_CURRENCY_CODE = "USD"
        const val MONTHLY_PERIOD_TYPE = "monthly"
    }
}
