package com.mojtaba.folentra.feature.dashboard.presentation

sealed interface DashboardAction {
    data object RetryClicked : DashboardAction
    data object SetBudgetClicked : DashboardAction
}
