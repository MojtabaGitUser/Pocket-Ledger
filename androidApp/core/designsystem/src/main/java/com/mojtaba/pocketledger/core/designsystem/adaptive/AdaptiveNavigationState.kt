package com.mojtaba.pocketledger.core.designsystem.adaptive

import androidx.compose.runtime.Immutable

@Immutable
data class AdaptiveNavigationState(
    val widthSizeClass: PocketLedgerWindowWidthSizeClass,
    val navigationType: AdaptiveNavigationType = adaptiveNavigationType(widthSizeClass),
    val paneType: AdaptivePaneType = adaptivePaneType(widthSizeClass),
)
