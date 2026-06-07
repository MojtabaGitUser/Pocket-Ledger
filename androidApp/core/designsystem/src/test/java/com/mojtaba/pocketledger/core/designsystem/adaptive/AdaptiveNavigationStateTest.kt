package com.mojtaba.pocketledger.core.designsystem.adaptive

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveNavigationStateTest {
    @Test
    fun mapsWindowWidthToSizeClass() {
        assertEquals(PocketLedgerWindowWidthSizeClass.Compact, pocketLedgerWindowWidthSizeClass(599.dp))
        assertEquals(PocketLedgerWindowWidthSizeClass.Medium, pocketLedgerWindowWidthSizeClass(600.dp))
        assertEquals(PocketLedgerWindowWidthSizeClass.Expanded, pocketLedgerWindowWidthSizeClass(840.dp))
    }

    @Test
    fun mapsSizeClassToNavigationType() {
        assertEquals(
            AdaptiveNavigationType.BottomBar,
            adaptiveNavigationType(PocketLedgerWindowWidthSizeClass.Compact),
        )
        assertEquals(
            AdaptiveNavigationType.NavigationRail,
            adaptiveNavigationType(PocketLedgerWindowWidthSizeClass.Medium),
        )
        assertEquals(
            AdaptiveNavigationType.PermanentDrawer,
            adaptiveNavigationType(PocketLedgerWindowWidthSizeClass.Expanded),
        )
    }

    @Test
    fun mapsSizeClassToListDetailSupport() {
        assertFalse(supportsListDetail(PocketLedgerWindowWidthSizeClass.Compact))
        assertTrue(supportsListDetail(PocketLedgerWindowWidthSizeClass.Medium))
        assertTrue(supportsListDetail(PocketLedgerWindowWidthSizeClass.Expanded))
    }

    @Test
    fun createsConsistentAdaptiveNavigationState() {
        val state = AdaptiveNavigationState(PocketLedgerWindowWidthSizeClass.Expanded)

        assertEquals(AdaptiveNavigationType.PermanentDrawer, state.navigationType)
        assertEquals(AdaptivePaneType.ListDetail, state.paneType)
    }
}
