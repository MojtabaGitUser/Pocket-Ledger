package com.mojtaba.pocketledger.debughealth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.pocketledger.PocketLedgerAppGraph

@Composable
fun DebugHealthScreen(
    appGraph: PocketLedgerAppGraph,
    modifier: Modifier = Modifier,
) {
    // Release and benchmark variants do not register the debug health route.
}
