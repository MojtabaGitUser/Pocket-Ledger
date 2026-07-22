package com.mojtaba.folentra.adaptive

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoldableStateTest {
    @Test
    fun flatFeatureMapsToFlatPosture() {
        val state = FoldableFeatureSnapshot(
            orientation = FoldableFeatureOrientation.Vertical,
            state = FoldableFeatureState.Flat,
            isSeparating = false,
        ).toFoldableUiState()

        assertEquals(FoldablePosture.Flat, state.posture)
        assertFalse(state.isSeparating)
    }

    @Test
    fun halfOpenedVerticalFeatureMapsToBookPosture() {
        val state = FoldableFeatureSnapshot(
            orientation = FoldableFeatureOrientation.Vertical,
            state = FoldableFeatureState.HalfOpened,
            isSeparating = true,
        ).toFoldableUiState()

        assertEquals(FoldablePosture.Book, state.posture)
        assertTrue(state.isSeparating)
    }

    @Test
    fun halfOpenedHorizontalFeatureMapsToTabletopPosture() {
        val state = FoldableFeatureSnapshot(
            orientation = FoldableFeatureOrientation.Horizontal,
            state = FoldableFeatureState.HalfOpened,
            isSeparating = true,
        ).toFoldableUiState()

        assertEquals(FoldablePosture.Tabletop, state.posture)
        assertTrue(state.isSeparating)
    }

    @Test
    fun unknownFeatureMapsToUnknownPosture() {
        val state = FoldableFeatureSnapshot(
            orientation = FoldableFeatureOrientation.Unknown,
            state = FoldableFeatureState.Unknown,
            isSeparating = false,
        ).toFoldableUiState()

        assertEquals(FoldablePosture.Unknown, state.posture)
    }
}
