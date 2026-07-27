package com.mojtaba.folentra.feature.dashboard.presentation

import com.mojtaba.folentra.core.designsystem.adaptive.FolentraWindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardLayoutModeTest {
    @Test
    fun compactUsesSingleColumnLayout() {
        assertEquals(
            DashboardLayoutMode.SingleColumn,
            dashboardLayoutMode(FolentraWindowWidthSizeClass.Compact),
        )
    }

    @Test
    fun mediumUsesTwoColumnLayout() {
        assertEquals(
            DashboardLayoutMode.TwoColumn,
            dashboardLayoutMode(FolentraWindowWidthSizeClass.Medium),
        )
    }

    @Test
    fun expandedUsesDashboardGridLayout() {
        assertEquals(
            DashboardLayoutMode.DashboardGrid,
            dashboardLayoutMode(FolentraWindowWidthSizeClass.Expanded),
        )
    }

    @Test
    fun twoHundredPercentFontScaleUsesRoomierLayouts() {
        assertEquals(
            DashboardLayoutMode.SingleColumn,
            dashboardLayoutMode(FolentraWindowWidthSizeClass.Medium, fontScale = 2f),
        )
        assertEquals(
            DashboardLayoutMode.TwoColumn,
            dashboardLayoutMode(FolentraWindowWidthSizeClass.Expanded, fontScale = 2f),
        )
    }
}
