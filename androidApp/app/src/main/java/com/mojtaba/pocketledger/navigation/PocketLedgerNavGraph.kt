package com.mojtaba.pocketledger.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink

@Composable
fun PocketLedgerNavGraph(
    navController: NavHostController,
    startDestination: AppDestination,
    includeDebugDestinations: Boolean,
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
        placeholderDestination(AppDestination.Transactions) {
            TransactionsPlaceholderScreen()
        }
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
