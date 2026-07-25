package com.mojtaba.folentra.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mojtaba.folentra.FolentraAppGraph
import com.mojtaba.folentra.core.designsystem.adaptive.AdaptiveNavigationType
import com.mojtaba.folentra.core.designsystem.adaptive.AdaptiveNavigationState
import com.mojtaba.folentra.feature.dashboard.navigation.DashboardRoutes
import com.mojtaba.folentra.feature.search.navigation.SearchRoutes
import com.mojtaba.folentra.feature.transaction.navigation.TransactionRoutes

@Composable
fun FolentraAppShell(
    appState: FolentraAppState,
    appGraph: FolentraAppGraph,
    adaptiveNavigationState: AdaptiveNavigationState,
    modifier: Modifier = Modifier,
) {
    val currentTopLevelDestination = appState.currentTopLevelDestination()
    val title = currentTopLevelDestination?.label ?: "Folentra"
    val visibleDestinations = appState.topLevelDestinations.filter { destination ->
        adaptiveNavigationState.navigationType != AdaptiveNavigationType.BottomBar ||
            destination.isPrimaryDestination
    }
    val navigationItems = visibleDestinations.map { destination ->
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
            FolentraNavGraph(
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

private val TopLevelDestination.isPrimaryDestination: Boolean
    get() = this != TopLevelDestination.DebugHealth &&
        this != TopLevelDestination.DebugFeatureFlags

@Composable
private fun FolentraAppState.shouldShowShellTopBar(): Boolean {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val currentRoute = currentDestination?.route
    return currentRoute != AppDestination.Dashboard.route &&
        currentRoute != DashboardRoutes.BudgetSetupRoute &&
        currentRoute != DashboardRoutes.BudgetEditRoutePattern &&
        currentRoute != SearchRoutes.SearchRoute &&
        currentRoute != TransactionRoutes.ListRoute &&
        currentRoute != TransactionRoutes.CreateRoute &&
        currentRoute != TransactionRoutes.DetailRoutePattern &&
        currentRoute != TransactionRoutes.EditRoutePattern
}
