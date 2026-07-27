package com.mojtaba.folentra.debugflags

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.folentra.FolentraAppGraph

@Composable
fun DebugFeatureFlagOverridesScreen(
    appGraph: FolentraAppGraph,
    modifier: Modifier = Modifier,
) {
    // Release and benchmark variants do not register the debug feature flag route.
}