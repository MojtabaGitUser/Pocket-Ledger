package com.mojtaba.folentra.feature.dashboard.presentation

import com.mojtaba.folentra.core.designsystem.adaptive.FolentraWindowWidthSizeClass

enum class DashboardLayoutMode {
    SingleColumn,
    TwoColumn,
    DashboardGrid,
}

fun dashboardLayoutMode(
    widthSizeClass: FolentraWindowWidthSizeClass,
    fontScale: Float = 1f,
): DashboardLayoutMode =
    if (fontScale >= 2f) {
        when (widthSizeClass) {
            FolentraWindowWidthSizeClass.Compact,
            FolentraWindowWidthSizeClass.Medium -> DashboardLayoutMode.SingleColumn
            FolentraWindowWidthSizeClass.Expanded -> DashboardLayoutMode.TwoColumn
        }
    } else {
        when (widthSizeClass) {
            FolentraWindowWidthSizeClass.Compact -> DashboardLayoutMode.SingleColumn
            FolentraWindowWidthSizeClass.Medium -> DashboardLayoutMode.TwoColumn
            FolentraWindowWidthSizeClass.Expanded -> DashboardLayoutMode.DashboardGrid
        }
    }
