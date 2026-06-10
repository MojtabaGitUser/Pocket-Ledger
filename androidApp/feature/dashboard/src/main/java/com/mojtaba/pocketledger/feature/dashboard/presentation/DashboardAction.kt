package com.mojtaba.pocketledger.feature.dashboard.presentation

sealed interface DashboardAction {
    data object RetryClicked : DashboardAction
    data object SetBudgetClicked : DashboardAction
}
