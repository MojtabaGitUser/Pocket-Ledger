package com.mojtaba.folentra

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.folentra.navigation.rememberFolentraAppState

@Composable
fun FolentraApp(
    appGraph: FolentraAppGraph = rememberFolentraAppGraph(),
    modifier: Modifier = Modifier,
) {
    val appState = rememberFolentraAppState(
        includeDebugDestinations = BuildConfig.DEBUG,
    )

    FolentraAdaptiveApp(
        appState = appState,
        appGraph = appGraph,
        modifier = modifier,
    )
}
