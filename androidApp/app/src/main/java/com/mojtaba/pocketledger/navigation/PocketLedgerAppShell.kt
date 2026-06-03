package com.mojtaba.pocketledger.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mojtaba.pocketledger.PocketLedgerAppGraph
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.transaction.navigation.TransactionRoutes

private val NavigationRailBreakpoint = 600.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketLedgerAppShell(
    appState: PocketLedgerAppState,
    appGraph: PocketLedgerAppGraph,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= NavigationRailBreakpoint
        val currentTopLevelDestination = appState.currentTopLevelDestination()
        val title = currentTopLevelDestination?.label ?: "Pocket Ledger"

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail) {
                PocketLedgerNavigationRail(
                    destinations = appState.topLevelDestinations,
                    currentDestination = currentTopLevelDestination,
                    onDestinationClick = appState::navigateToTopLevelDestination,
                )
            }

            val showShellTopBar = appState.shouldShowShellTopBar()

            Scaffold(
                modifier = Modifier.weight(1f),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    if (showShellTopBar) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        PocketLedgerNavigationBar(
                            destinations = appState.topLevelDestinations,
                            currentDestination = currentTopLevelDestination,
                            onDestinationClick = appState::navigateToTopLevelDestination,
                        )
                    }
                },
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
    }
}

@Composable
private fun PocketLedgerAppState.shouldShowShellTopBar(): Boolean {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val currentRoute = currentDestination?.route
    return currentRoute != TransactionRoutes.ListRoute &&
        currentRoute != TransactionRoutes.DetailRoutePattern &&
        currentRoute != TransactionRoutes.EditRoutePattern
}

@Composable
private fun PocketLedgerNavigationBar(
    destinations: List<TopLevelDestination>,
    currentDestination: TopLevelDestination?,
    onDestinationClick: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = destination == currentDestination,
                onClick = { onDestinationClick(destination) },
                icon = {
                    DestinationIcon(label = destination.shortLabel)
                },
                label = {
                    Text(text = destination.label)
                },
                alwaysShowLabel = false,
            )
        }
    }
}

@Composable
private fun PocketLedgerNavigationRail(
    destinations: List<TopLevelDestination>,
    currentDestination: TopLevelDestination?,
    onDestinationClick: (TopLevelDestination) -> Unit,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    NavigationRail(
        modifier = Modifier.padding(vertical = spacing.small),
    ) {
        destinations.forEach { destination ->
            NavigationRailItem(
                selected = destination == currentDestination,
                onClick = { onDestinationClick(destination) },
                icon = {
                    DestinationIcon(label = destination.shortLabel)
                },
                label = {
                    Text(text = destination.label)
                },
                alwaysShowLabel = false,
            )
        }
    }
}

@Composable
private fun DestinationIcon(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
    )
}
