package com.mojtaba.folentra.feature.dashboard.budget

sealed interface BudgetSetupAction {
    data class NameChanged(val value: String) : BudgetSetupAction
    data class AmountChanged(val value: String) : BudgetSetupAction
    data class CurrencyChanged(val value: String) : BudgetSetupAction
    data class CategorySelected(val categoryId: String?) : BudgetSetupAction
    data class PeriodChanged(
        val periodStart: Long?,
        val periodEnd: Long?,
    ) : BudgetSetupAction
    data class ActiveChanged(val value: Boolean) : BudgetSetupAction
    data object SaveClicked : BudgetSetupAction
}
