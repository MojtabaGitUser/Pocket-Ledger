package com.mojtaba.pocketledger.feature.dashboard.presentation

import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardLayoutModeTest {
    @Test
    fun compactUsesSingleColumnLayout() {
        assertEquals(
            DashboardLayoutMode.SingleColumn,
            dashboardLayoutMode(PocketLedgerWindowWidthSizeClass.Compact),
        )
    }

    @Test
    fun mediumUsesTwoColumnLayout() {
        assertEquals(
            DashboardLayoutMode.TwoColumn,
            dashboardLayoutMode(PocketLedgerWindowWidthSizeClass.Medium),
        )
    }

    @Test
    fun expandedUsesDashboardGridLayout() {
        assertEquals(
            DashboardLayoutMode.DashboardGrid,
            dashboardLayoutMode(PocketLedgerWindowWidthSizeClass.Expanded),
        )
    }
}
