package com.mojtaba.folentra.feature.transaction.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.designsystem.adaptive.AdaptivePaneType
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TagRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.feature.transaction.adaptive.TransactionAdaptiveRoute
import com.mojtaba.folentra.feature.transaction.form.TransactionFormMode
import com.mojtaba.folentra.feature.transaction.presentation.detail.TransactionDetailRoute
import com.mojtaba.folentra.feature.transaction.presentation.editor.TransactionEditorRoute
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListRoute

fun NavGraphBuilder.transactionGraph(
    navController: NavHostController,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
    aiFallbackStrategy: AiFallbackStrategy? = null,
    deepLinkBaseUri: String,
    paneType: AdaptivePaneType = AdaptivePaneType.SinglePane,
) {
    composable(
        route = TransactionRoutes.ListRoute,
        deepLinks = listOf(
            navDeepLink { uriPattern = "$deepLinkBaseUri/${TransactionRoutes.ListRoute}" },
        ),
    ) {
        if (paneType == AdaptivePaneType.ListDetail) {
            TransactionAdaptiveRoute(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                tagRepository = tagRepository,
                initialSelectedTransactionId = null,
                onAddTransaction = { navController.navigate(TransactionRoutes.CreateRoute) },
                onEditTransaction = { id -> navController.navigate(TransactionRoutes.editRoute(id)) },
            )
        } else {
            TransactionListRoute(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                tagRepository = tagRepository,
                onOpenTransaction = { transactionId ->
                    navController.navigate(TransactionRoutes.detailRoute(transactionId))
                },
                onAddTransaction = { navController.navigate(TransactionRoutes.CreateRoute) },
            )
        }
    }

    composable(route = TransactionRoutes.CreateRoute) {
        TransactionEditorRoute(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            aiFallbackStrategy = aiFallbackStrategy,
            mode = TransactionFormMode.CREATE,
            transactionId = null,
            onNavigateBack = navController::navigateUp,
            onSaved = { navController.popBackStack() },
        )
    }

    composable(
        route = TransactionRoutes.DetailRoutePattern,
        arguments = listOf(
            navArgument(TransactionRoutes.TransactionIdArg) {
                type = NavType.StringType
            },
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "$deepLinkBaseUri/transactions/detail/{${TransactionRoutes.TransactionIdArg}}"
            },
        ),
    ) { backStackEntry ->
        val transactionId = backStackEntry.arguments?.getString(TransactionRoutes.TransactionIdArg)
        if (paneType == AdaptivePaneType.ListDetail) {
            TransactionAdaptiveRoute(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                tagRepository = tagRepository,
                initialSelectedTransactionId = transactionId,
                onAddTransaction = { navController.navigate(TransactionRoutes.CreateRoute) },
                onEditTransaction = { id -> navController.navigate(TransactionRoutes.editRoute(id)) },
            )
        } else {
            TransactionDetailRoute(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                tagRepository = tagRepository,
                transactionId = transactionId,
                onNavigateBack = navController::navigateUp,
                onEditTransaction = { id -> navController.navigate(TransactionRoutes.editRoute(id)) },
            )
        }
    }

    composable(
        route = TransactionRoutes.EditRoutePattern,
        arguments = listOf(
            navArgument(TransactionRoutes.TransactionIdArg) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->
        val transactionId = backStackEntry.arguments?.getString(TransactionRoutes.TransactionIdArg)
        TransactionEditorRoute(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            aiFallbackStrategy = aiFallbackStrategy,
            mode = TransactionFormMode.EDIT,
            transactionId = transactionId,
            onNavigateBack = navController::navigateUp,
            onSaved = {
                navController.popBackStack()
            },
        )
    }
}
