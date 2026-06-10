package com.mojtaba.pocketledger.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mojtaba.pocketledger.PocketLedgerAppGraph
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptiveNavigationState
import com.mojtaba.pocketledger.feature.dashboard.navigation.DashboardRoutes
import com.mojtaba.pocketledger.feature.search.navigation.SearchRoutes
import com.mojtaba.pocketledger.feature.transaction.navigation.TransactionRoutes

@Composable
fun PocketLedgerAppShell(
    appState: PocketLedgerAppState,
    appGraph: PocketLedgerAppGraph,
    adaptiveNavigationState: AdaptiveNavigationState,
    modifier: Modifier = Modifier,
) {
    val currentTopLevelDestination = appState.currentTopLevelDestination()
    val title = currentTopLevelDestination?.label ?: "Pocket Ledger"
    val navigationItems = appState.topLevelDestinations.map { destination ->
        AdaptiveNavigationItem(
            label = destination.label,
            shortLabel = destination.shortLabel,
            selected = destination == currentTopLevelDestination,
            contentDescription = "${destination.label} navigation destination",
            onClick = { appState.navigateToTopLevelDestination(destination) },
        )
    }

    AdaptiveNavigationScaffold(
        navigationType = adaptiveNavigationState.navigationType,
        navigationItems = navigationItems,
        title = title,
        showTopBar = appState.shouldShowShellTopBar(),
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PocketLedgerNavGraph(
                navController = appState.navController,
                startDestination = appState.startDestination,
                includeDebugDestinations = appState.topLevelDestinations
                    .contains(TopLevelDestination.DebugHealth),
                appGraph = appGraph,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PocketLedgerAppState.shouldShowShellTopBar(): Boolean {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val currentRoute = currentDestination?.route
    return currentRoute != AppDestination.Dashboard.route &&
        currentRoute != DashboardRoutes.BudgetSetupRoute &&
        currentRoute != DashboardRoutes.BudgetEditRoutePattern &&
        currentRoute != SearchRoutes.SearchRoute &&
        currentRoute != TransactionRoutes.ListRoute &&
        currentRoute != TransactionRoutes.DetailRoutePattern &&
        currentRoute != TransactionRoutes.EditRoutePattern
}
