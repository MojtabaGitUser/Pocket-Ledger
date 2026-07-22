package com.mojtaba.folentra.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mojtaba.folentra.core.database.createFolentraDatabase
import com.mojtaba.folentra.desktop.insights.DesktopInsightsScreen
import com.mojtaba.folentra.desktop.insights.DesktopInsightsStateMapper
import com.mojtaba.folentra.desktop.insights.RuleBasedDesktopInsightsProvider
import com.mojtaba.folentra.desktop.persistence.DesktopLedgerLocalDataSource
import com.mojtaba.folentra.desktop.persistence.DesktopLedgerSnapshot
import com.mojtaba.folentra.desktop.search.DesktopSearchScreen
import com.mojtaba.folentra.desktop.theme.FolentraDesktopTheme

@Composable
fun FolentraDesktopApp() {
    val database = remember { createFolentraDatabase() }
    DisposableEffect(database) {
        onDispose { database.close() }
    }
    val ledgerSnapshot by produceState<DesktopLedgerSnapshot?>(initialValue = null, database) {
        value = DesktopLedgerLocalDataSource(database).loadSnapshot()
    }

    FolentraDesktopTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            var destination by remember { mutableStateOf(DesktopDestination.Insights) }
            val insightsMapper = remember {
                DesktopInsightsStateMapper(RuleBasedDesktopInsightsProvider())
            }
            val snapshot = ledgerSnapshot

            Row(modifier = Modifier.fillMaxSize()) {
                DesktopNavigationRail(
                    selected = destination,
                    onDestinationSelected = { destination = it },
                )
                if (snapshot == null) {
                    DesktopLoadingScreen(modifier = Modifier.weight(1f))
                } else {
                    when (destination) {
                        DesktopDestination.Dashboard -> DesktopPlaceholderScreen("Dashboard")
                        DesktopDestination.Search -> DesktopSearchScreen(
                            records = snapshot.searchRecords,
                            modifier = Modifier.weight(1f),
                        )
                        DesktopDestination.Insights -> DesktopInsightsScreen(
                            state = insightsMapper.map(snapshot.monthlySummary),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopNavigationRail(
    selected: DesktopDestination,
    onDestinationSelected: (DesktopDestination) -> Unit,
) {
    NavigationRail(
        modifier = Modifier.semantics {
            contentDescription = "Desktop demo navigation"
        },
    ) {
        DesktopDestination.entries.forEach { destination ->
            NavigationRailItem(
                selected = destination == selected,
                onClick = { onDestinationSelected(destination) },
                icon = { Text(destination.shortLabel) },
                label = { Text(destination.label) },
                modifier = Modifier.semantics {
                    contentDescription = "${destination.label} navigation item"
                },
            )
        }
    }
}

@Composable
private fun DesktopLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Loading local ledger",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun DesktopPlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$title desktop demo placeholder",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

private enum class DesktopDestination(
    val label: String,
    val shortLabel: String,
) {
    Dashboard("Dashboard", "D"),
    Search("Search", "S"),
    Insights("Insights", "I"),
}
