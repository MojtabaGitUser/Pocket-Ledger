package com.mojtaba.folentra.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.data.repository.BudgetRepository
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.core.designsystem.adaptive.FolentraWindowWidthSizeClass
import com.mojtaba.folentra.feature.dashboard.budget.BudgetSetupMode
import com.mojtaba.folentra.feature.dashboard.budget.BudgetSetupRoute
import com.mojtaba.folentra.feature.dashboard.presentation.DashboardStateRoute

fun NavGraphBuilder.dashboardGraph(
    navController: NavHostController,
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    aiFallbackStrategy: AiFallbackStrategy,
    deepLinkBaseUri: String,
    widthSizeClass: FolentraWindowWidthSizeClass = FolentraWindowWidthSizeClass.Compact,
) {
    composable(
        route = DashboardRoutes.DashboardRoute,
        deepLinks = listOf(
            navDeepLink { uriPattern = "$deepLinkBaseUri/${DashboardRoutes.DashboardRoute}" },
        ),
    ) {
        DashboardStateRoute(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            aiFallbackStrategy = aiFallbackStrategy,
            widthSizeClass = widthSizeClass,
            onSetBudget = { navController.navigate(DashboardRoutes.BudgetSetupRoute) },
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
