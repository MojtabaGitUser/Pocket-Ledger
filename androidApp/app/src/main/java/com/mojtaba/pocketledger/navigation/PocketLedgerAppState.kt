package com.mojtaba.pocketledger.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberPocketLedgerAppState(
    navController: NavHostController = rememberNavController(),
    includeDebugDestinations: Boolean,
): PocketLedgerAppState =
    remember(navController, includeDebugDestinations) {
        PocketLedgerAppState(
            navController = navController,
            includeDebugDestinations = includeDebugDestinations,
        )
    }

@Stable
class PocketLedgerAppState(
    val navController: NavHostController,
    includeDebugDestinations: Boolean,
) {
    val topLevelDestinations: List<TopLevelDestination> =
        buildList {
            add(TopLevelDestination.Dashboard)
            add(TopLevelDestination.Transactions)
            add(TopLevelDestination.Search)
            add(TopLevelDestination.Insights)
            add(TopLevelDestination.Settings)
            if (includeDebugDestinations) {
                add(TopLevelDestination.DebugHealth)
            }
        }

    val startDestination: AppDestination = AppDestination.Dashboard

    @Composable
    fun currentTopLevelDestination(): TopLevelDestination? {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        return topLevelDestinations.firstOrNull { destination ->
            currentDestination.isRouteInHierarchy(destination.destination.route)
        }
    }

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        navController.navigate(topLevelDestination.destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true
