package com.mojtaba.pocketledger.feature.transaction.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.feature.transaction.form.TransactionFormMode
import com.mojtaba.pocketledger.feature.transaction.presentation.detail.TransactionDetailRoute
import com.mojtaba.pocketledger.feature.transaction.presentation.editor.TransactionEditorRoute
import com.mojtaba.pocketledger.feature.transaction.presentation.list.TransactionListRoute

fun NavGraphBuilder.transactionGraph(
    navController: NavHostController,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
    deepLinkBaseUri: String,
) {
    composable(
        route = TransactionRoutes.ListRoute,
        deepLinks = listOf(
            navDeepLink { uriPattern = "$deepLinkBaseUri/${TransactionRoutes.ListRoute}" },
        ),
    ) {
        TransactionListRoute(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            onOpenTransaction = { transactionId ->
                navController.navigate(TransactionRoutes.detailRoute(transactionId))
            },
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
        TransactionDetailRoute(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            transactionId = transactionId,
            onNavigateBack = navController::navigateUp,
            onEditTransaction = { id -> navController.navigate(TransactionRoutes.editRoute(id)) },
        )
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
            mode = TransactionFormMode.EDIT,
            transactionId = transactionId,
            onNavigateBack = navController::navigateUp,
            onSaved = {
                navController.popBackStack()
            },
        )
    }
}
