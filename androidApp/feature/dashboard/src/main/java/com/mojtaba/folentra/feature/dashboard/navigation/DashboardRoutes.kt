package com.mojtaba.folentra.feature.dashboard.navigation

import android.net.Uri

object DashboardRoutes {
    const val DashboardRoute = "dashboard"
    const val BudgetIdArg = "budgetId"
    const val BudgetSetupRoute = "dashboard/budgets/setup"
    const val BudgetEditRoutePattern = "dashboard/budgets/edit/{$BudgetIdArg}"

    fun editBudgetRoute(budgetId: String): String =
        "dashboard/budgets/edit/${Uri.encode(budgetId)}"
}
