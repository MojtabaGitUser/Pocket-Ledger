package com.mojtaba.pocketledger.adaptive

import androidx.compose.runtime.staticCompositionLocalOf
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptiveNavigationState
import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass

val LocalAdaptiveNavigationState = staticCompositionLocalOf {
    AdaptiveNavigationState(PocketLedgerWindowWidthSizeClass.Compact)
}

val LocalFoldableUiState = staticCompositionLocalOf {
    FoldableUiState.Flat
}
