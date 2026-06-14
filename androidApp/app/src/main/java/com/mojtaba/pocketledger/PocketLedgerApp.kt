package com.mojtaba.pocketledger

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.pocketledger.navigation.rememberPocketLedgerAppState

@Composable
fun PocketLedgerApp(
    appGraph: PocketLedgerAppGraph = rememberPocketLedgerAppGraph(),
    modifier: Modifier = Modifier,
) {
    val appState = rememberPocketLedgerAppState(
        includeDebugDestinations = BuildConfig.DEBUG,
    )

    PocketLedgerAdaptiveApp(
        appState = appState,
        appGraph = appGraph,
        modifier = modifier,
    )
}
