package com.mojtaba.pocketledger.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass

@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    uiState: DashboardUiState = DashboardUiState.Empty,
    widthSizeClass: PocketLedgerWindowWidthSizeClass = PocketLedgerWindowWidthSizeClass.Compact,
    onAction: (DashboardAction) -> Unit = {},
) {
    DashboardScreen(
        uiState = uiState,
        widthSizeClass = widthSizeClass,
        onAction = onAction,
        modifier = modifier,
    )
}
