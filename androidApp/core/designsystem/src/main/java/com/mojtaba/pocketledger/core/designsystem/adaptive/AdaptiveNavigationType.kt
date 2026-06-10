package com.mojtaba.pocketledger.core.designsystem.adaptive

enum class AdaptiveNavigationType {
    BottomBar,
    NavigationRail,
    PermanentDrawer,
}

fun adaptiveNavigationType(
    widthSizeClass: PocketLedgerWindowWidthSizeClass,
): AdaptiveNavigationType =
    when (widthSizeClass) {
        PocketLedgerWindowWidthSizeClass.Compact -> AdaptiveNavigationType.BottomBar
        PocketLedgerWindowWidthSizeClass.Medium -> AdaptiveNavigationType.NavigationRail
        PocketLedgerWindowWidthSizeClass.Expanded -> AdaptiveNavigationType.PermanentDrawer
    }
