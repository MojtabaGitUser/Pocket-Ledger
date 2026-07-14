package com.mojtaba.pocketledger.debugflags

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.pocketledger.PocketLedgerAppGraph

@Composable
fun DebugFeatureFlagOverridesScreen(
    appGraph: PocketLedgerAppGraph,
    modifier: Modifier = Modifier,
) {
    // Release and benchmark variants do not register the debug feature flag route.
}