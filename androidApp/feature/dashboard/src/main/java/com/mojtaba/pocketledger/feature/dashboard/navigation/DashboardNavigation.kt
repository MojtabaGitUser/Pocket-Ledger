package com.mojtaba.pocketledger.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mojtaba.pocketledger.core.ai.AiFallbackStrategy
import com.mojtaba.pocketledger.core.data.repository.BudgetRepository
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass
import com.mojtaba.pocketledger.feature.dashboard.budget.BudgetSetupMode
import com.mojtaba.pocketledger.feature.dashboard.budget.BudgetSetupRoute
import com.mojtaba.pocketledger.feature.dashboard.presentation.DashboardStateRoute

fun NavGraphBuilder.dashboardGraph(
    navController: NavHostController,
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    aiFallbackStrategy: AiFallbackStrategy,
    deepLinkBaseUri: String,
    widthSizeClass: PocketLedgerWindowWidthSizeClass = PocketLedgerWindowWidthSizeClass.Compact,
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
