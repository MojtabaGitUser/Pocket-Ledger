package com.mojtaba.folentra.debughealth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.folentra.FolentraAppGraph

@Composable
fun DebugHealthScreen(
    appGraph: FolentraAppGraph,
    modifier: Modifier = Modifier,
) {
    // Release and benchmark variants do not register the debug health route.
}
