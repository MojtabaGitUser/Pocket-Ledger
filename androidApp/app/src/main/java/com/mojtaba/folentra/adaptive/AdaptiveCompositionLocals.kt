package com.mojtaba.folentra.adaptive

import androidx.compose.runtime.staticCompositionLocalOf
import com.mojtaba.folentra.core.designsystem.adaptive.AdaptiveNavigationState
import com.mojtaba.folentra.core.designsystem.adaptive.FolentraWindowWidthSizeClass

val LocalAdaptiveNavigationState = staticCompositionLocalOf {
    AdaptiveNavigationState(FolentraWindowWidthSizeClass.Compact)
}

val LocalFoldableUiState = staticCompositionLocalOf {
    FoldableUiState.Flat
}
