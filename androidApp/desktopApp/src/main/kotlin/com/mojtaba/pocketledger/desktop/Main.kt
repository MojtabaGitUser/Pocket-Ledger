package com.mojtaba.pocketledger.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Pocket Ledger Desktop Demo",
    ) {
        PocketLedgerDesktopApp()
    }
}
