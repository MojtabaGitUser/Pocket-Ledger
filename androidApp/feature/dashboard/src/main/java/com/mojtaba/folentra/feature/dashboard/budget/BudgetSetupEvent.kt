package com.mojtaba.folentra.feature.dashboard.budget

sealed interface BudgetSetupEvent {
    data object SaveCompleted : BudgetSetupEvent
    data class ShowSnackbar(val message: String) : BudgetSetupEvent
}
