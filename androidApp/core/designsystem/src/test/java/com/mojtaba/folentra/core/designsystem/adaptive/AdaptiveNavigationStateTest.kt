package com.mojtaba.folentra.core.designsystem.adaptive

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveNavigationStateTest {
    @Test
    fun mapsWindowWidthToSizeClass() {
        assertEquals(FolentraWindowWidthSizeClass.Compact, folentraWindowWidthSizeClass(599.dp))
        assertEquals(FolentraWindowWidthSizeClass.Medium, folentraWindowWidthSizeClass(600.dp))
        assertEquals(FolentraWindowWidthSizeClass.Expanded, folentraWindowWidthSizeClass(840.dp))
    }

    @Test
    fun mapsSizeClassToNavigationType() {
        assertEquals(
            AdaptiveNavigationType.BottomBar,
            adaptiveNavigationType(FolentraWindowWidthSizeClass.Compact),
        )
        assertEquals(
            AdaptiveNavigationType.NavigationRail,
            adaptiveNavigationType(FolentraWindowWidthSizeClass.Medium),
        )
        assertEquals(
            AdaptiveNavigationType.PermanentDrawer,
            adaptiveNavigationType(FolentraWindowWidthSizeClass.Expanded),
        )
    }

    @Test
    fun mapsSizeClassToListDetailSupport() {
        assertFalse(supportsListDetail(FolentraWindowWidthSizeClass.Compact))
        assertTrue(supportsListDetail(FolentraWindowWidthSizeClass.Medium))
        assertTrue(supportsListDetail(FolentraWindowWidthSizeClass.Expanded))
    }

    @Test
    fun createsConsistentAdaptiveNavigationState() {
        val state = AdaptiveNavigationState(FolentraWindowWidthSizeClass.Expanded)

        assertEquals(AdaptiveNavigationType.PermanentDrawer, state.navigationType)
        assertEquals(AdaptivePaneType.ListDetail, state.paneType)
    }
}
