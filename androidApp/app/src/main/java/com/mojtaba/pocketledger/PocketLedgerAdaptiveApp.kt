package com.mojtaba.pocketledger

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mojtaba.pocketledger.adaptive.LocalAdaptiveNavigationState
import com.mojtaba.pocketledger.adaptive.LocalFoldableUiState
import com.mojtaba.pocketledger.adaptive.adaptiveNavigationStateForWidth
import com.mojtaba.pocketledger.adaptive.rememberFoldableUiState
import com.mojtaba.pocketledger.navigation.PocketLedgerAppShell
import com.mojtaba.pocketledger.navigation.PocketLedgerAppState

@Composable
fun PocketLedgerAdaptiveApp(
    appState: PocketLedgerAppState,
    appGraph: PocketLedgerAppGraph,
    modifier: Modifier = Modifier,
) {
    val foldableUiState by rememberFoldableUiState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val adaptiveNavigationState = adaptiveNavigationStateForWidth(maxWidth)
        CompositionLocalProvider(
            LocalAdaptiveNavigationState provides adaptiveNavigationState,
            LocalFoldableUiState provides foldableUiState,
        ) {
            PocketLedgerAppShell(
                appState = appState,
                appGraph = appGraph,
                adaptiveNavigationState = adaptiveNavigationState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
