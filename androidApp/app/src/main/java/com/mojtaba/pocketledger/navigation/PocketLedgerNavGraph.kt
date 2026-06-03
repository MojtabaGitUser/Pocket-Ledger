package com.mojtaba.pocketledger.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.mojtaba.pocketledger.PocketLedgerAppGraph
import com.mojtaba.pocketledger.feature.transaction.navigation.transactionGraph

@Composable
fun PocketLedgerNavGraph(
    navController: NavHostController,
    startDestination: AppDestination,
    includeDebugDestinations: Boolean,
    appGraph: PocketLedgerAppGraph,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
    ) {
        placeholderDestination(AppDestination.Dashboard) {
            DashboardPlaceholderScreen()
        }
        transactionGraph(
            navController = navController,
            transactionRepository = appGraph.transactionRepository,
            categoryRepository = appGraph.categoryRepository,
            tagRepository = appGraph.tagRepository,
            deepLinkBaseUri = "${AppDestination.DEEP_LINK_SCHEME}://${AppDestination.DEEP_LINK_HOST}",
        )
        placeholderDestination(AppDestination.Search) {
            SearchPlaceholderScreen()
        }
        placeholderDestination(AppDestination.Insights) {
            InsightsPlaceholderScreen()
        }
        placeholderDestination(AppDestination.Settings) {
            SettingsPlaceholderScreen()
        }
        if (includeDebugDestinations) {
            placeholderDestination(AppDestination.DebugHealth) {
                DebugHealthPlaceholderScreen()
            }
        }
    }
}

private fun NavGraphBuilder.placeholderDestination(
    destination: AppDestination,
    content: @Composable () -> Unit,
) {
    composable(
        route = destination.route,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = AppDestination.deepLinkUri(destination)
            },
        ),
    ) {
        content()
    }
}
