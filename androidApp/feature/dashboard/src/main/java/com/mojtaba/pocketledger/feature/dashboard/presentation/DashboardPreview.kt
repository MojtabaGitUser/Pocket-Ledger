package com.mojtaba.pocketledger.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import com.mojtaba.pocketledger.feature.dashboard.presentation.preview.DashboardPreviewFixtures

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun DashboardLoadingPreview() {
    PocketLedgerPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Loading,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun DashboardEmptyPreview() {
    PocketLedgerPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Empty,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun DashboardErrorPreview() {
    PocketLedgerPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Error("The local dashboard summary could not be read."),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun DashboardPhoneContentPreview() {
    PocketLedgerPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun DashboardEmptyListsContentPreview() {
    PocketLedgerPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.emptySummary),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 900, heightDp = 760)
@Composable
private fun DashboardTabletContentPreview() {
    PocketLedgerPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            onAction = {},
        )
    }
}
