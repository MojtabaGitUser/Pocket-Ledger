package com.mojtaba.pocketledger.adaptive

import androidx.compose.ui.unit.dp
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptiveNavigationType
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptivePaneType
import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Test

class WindowSizeClassProviderTest {
    @Test
    fun compactWidthCreatesBottomBarSinglePaneState() {
        val state = adaptiveNavigationStateForWidth(360.dp)

        assertEquals(PocketLedgerWindowWidthSizeClass.Compact, state.widthSizeClass)
        assertEquals(AdaptiveNavigationType.BottomBar, state.navigationType)
        assertEquals(AdaptivePaneType.SinglePane, state.paneType)
    }

    @Test
    fun mediumWidthCreatesRailListDetailState() {
        val state = adaptiveNavigationStateForWidth(700.dp)

        assertEquals(PocketLedgerWindowWidthSizeClass.Medium, state.widthSizeClass)
        assertEquals(AdaptiveNavigationType.NavigationRail, state.navigationType)
        assertEquals(AdaptivePaneType.ListDetail, state.paneType)
    }

    @Test
    fun expandedWidthCreatesDrawerListDetailState() {
        val state = adaptiveNavigationStateForWidth(1000.dp)

        assertEquals(PocketLedgerWindowWidthSizeClass.Expanded, state.widthSizeClass)
        assertEquals(AdaptiveNavigationType.PermanentDrawer, state.navigationType)
        assertEquals(AdaptivePaneType.ListDetail, state.paneType)
    }
}
