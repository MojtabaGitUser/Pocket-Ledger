package com.mojtaba.pocketledger.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mojtaba.pocketledger.core.data.repository.BudgetRepository
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.feature.dashboard.budget.BudgetSetupMode
import com.mojtaba.pocketledger.feature.dashboard.budget.BudgetSetupRoute
import com.mojtaba.pocketledger.feature.dashboard.presentation.DashboardAction
import com.mojtaba.pocketledger.feature.dashboard.presentation.DashboardRoute

fun NavGraphBuilder.dashboardGraph(
    navController: NavHostController,
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    deepLinkBaseUri: String,
) {
    composable(
        route = DashboardRoutes.DashboardRoute,
        deepLinks = listOf(
            navDeepLink { uriPattern = "$deepLinkBaseUri/${DashboardRoutes.DashboardRoute}" },
        ),
    ) {
        DashboardRoute(
            onAction = { action ->
                when (action) {
                    DashboardAction.RetryClicked -> Unit
                    DashboardAction.SetBudgetClicked -> navController.navigate(DashboardRoutes.BudgetSetupRoute)
                }
            },
        )
    }

    composable(route = DashboardRoutes.BudgetSetupRoute) {
        BudgetSetupRoute(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            mode = BudgetSetupMode.CREATE,
            budgetId = null,
            onNavigateBack = navController::navigateUp,
            onSaved = { navController.popBackStack() },
        )
    }

    composable(
        route = DashboardRoutes.BudgetEditRoutePattern,
        arguments = listOf(
            navArgument(DashboardRoutes.BudgetIdArg) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->
        val budgetId = backStackEntry.arguments?.getString(DashboardRoutes.BudgetIdArg)
        BudgetSetupRoute(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            mode = BudgetSetupMode.EDIT,
            budgetId = budgetId,
            onNavigateBack = navController::navigateUp,
            onSaved = { navController.popBackStack() },
        )
    }
}
