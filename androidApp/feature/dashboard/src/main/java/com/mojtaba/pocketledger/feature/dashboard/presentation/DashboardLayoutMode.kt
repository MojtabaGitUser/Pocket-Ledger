package com.mojtaba.pocketledger.feature.dashboard.presentation

import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass

enum class DashboardLayoutMode {
    SingleColumn,
    TwoColumn,
    DashboardGrid,
}

fun dashboardLayoutMode(
    widthSizeClass: PocketLedgerWindowWidthSizeClass,
): DashboardLayoutMode =
    when (widthSizeClass) {
        PocketLedgerWindowWidthSizeClass.Compact -> DashboardLayoutMode.SingleColumn
        PocketLedgerWindowWidthSizeClass.Medium -> DashboardLayoutMode.TwoColumn
        PocketLedgerWindowWidthSizeClass.Expanded -> DashboardLayoutMode.DashboardGrid
    }
