package com.mojtaba.pocketledger

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import com.mojtaba.pocketledger.navigation.PocketLedgerAppShell
import com.mojtaba.pocketledger.navigation.rememberPocketLedgerAppState

@Composable
fun PocketLedgerApp(
    modifier: Modifier = Modifier,
) {
    val appState = rememberPocketLedgerAppState(
        includeDebugDestinations = BuildConfig.DEBUG,
    )

    PocketLedgerAppShell(
        appState = appState,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun PocketLedgerAppPreview() {
    PocketLedgerPreviewTheme {
        PocketLedgerApp()
    }
}
