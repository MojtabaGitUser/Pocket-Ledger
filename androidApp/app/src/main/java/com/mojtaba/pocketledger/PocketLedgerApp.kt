package com.mojtaba.pocketledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketLedgerApp() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Pocket Ledger")
                },
            )
        },
    ) { innerPadding ->
        val spacing = PocketLedgerThemeDefaults.spacing

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Your shared Material 3 theme system is active.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PocketLedgerAppPreview() {
    PocketLedgerPreviewTheme {
        PocketLedgerApp()
    }
}
