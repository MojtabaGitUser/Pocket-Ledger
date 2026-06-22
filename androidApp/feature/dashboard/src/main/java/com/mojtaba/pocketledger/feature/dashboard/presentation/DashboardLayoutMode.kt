package com.mojtaba.pocketledger.feature.dashboard.presentation

import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass

enum class DashboardLayoutMode {
    SingleColumn,
    TwoColumn,
    DashboardGrid,
}

fun dashboardLayoutMode(
    widthSizeClass: PocketLedgerWindowWidthSizeClass,
    fontScale: Float = 1f,
): DashboardLayoutMode =
    if (fontScale >= 2f) {
        when (widthSizeClass) {
            PocketLedgerWindowWidthSizeClass.Compact,
            PocketLedgerWindowWidthSizeClass.Medium -> DashboardLayoutMode.SingleColumn
            PocketLedgerWindowWidthSizeClass.Expanded -> DashboardLayoutMode.TwoColumn
        }
    } else {
        when (widthSizeClass) {
            PocketLedgerWindowWidthSizeClass.Compact -> DashboardLayoutMode.SingleColumn
            PocketLedgerWindowWidthSizeClass.Medium -> DashboardLayoutMode.TwoColumn
            PocketLedgerWindowWidthSizeClass.Expanded -> DashboardLayoutMode.DashboardGrid
        }
    }
