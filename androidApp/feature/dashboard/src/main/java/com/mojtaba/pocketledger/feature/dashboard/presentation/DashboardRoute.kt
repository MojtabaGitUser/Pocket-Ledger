package com.mojtaba.pocketledger.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    uiState: DashboardUiState = DashboardUiState.Empty,
    onAction: (DashboardAction) -> Unit = {},
) {
    DashboardScreen(
        uiState = uiState,
        onAction = onAction,
        modifier = modifier,
    )
}
