package com.mojtaba.folentra

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mojtaba.folentra.adaptive.LocalAdaptiveNavigationState
import com.mojtaba.folentra.adaptive.LocalFoldableUiState
import com.mojtaba.folentra.adaptive.adaptiveNavigationStateForWidth
import com.mojtaba.folentra.adaptive.rememberFoldableUiState
import com.mojtaba.folentra.navigation.FolentraAppShell
import com.mojtaba.folentra.navigation.FolentraAppState
import com.mojtaba.folentra.security.AppLockGate

@Composable
fun FolentraAdaptiveApp(
    appState: FolentraAppState,
    appGraph: FolentraAppGraph,
    modifier: Modifier = Modifier,
) {
    val foldableUiState by rememberFoldableUiState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val adaptiveNavigationState = adaptiveNavigationStateForWidth(maxWidth)
        CompositionLocalProvider(
            LocalAdaptiveNavigationState provides adaptiveNavigationState,
            LocalFoldableUiState provides foldableUiState,
        ) {
            AppLockGate(
                appLockManager = appGraph.appLockManager,
                modifier = Modifier.fillMaxSize(),
            ) {
                FolentraAppShell(
                    appState = appState,
                    appGraph = appGraph,
                    adaptiveNavigationState = adaptiveNavigationState,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
