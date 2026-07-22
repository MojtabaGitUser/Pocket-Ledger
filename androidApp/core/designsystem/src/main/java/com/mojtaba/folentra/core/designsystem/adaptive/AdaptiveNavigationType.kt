package com.mojtaba.folentra.core.designsystem.adaptive

enum class AdaptiveNavigationType {
    BottomBar,
    NavigationRail,
    PermanentDrawer,
}

fun adaptiveNavigationType(
    widthSizeClass: FolentraWindowWidthSizeClass,
): AdaptiveNavigationType =
    when (widthSizeClass) {
        FolentraWindowWidthSizeClass.Compact -> AdaptiveNavigationType.BottomBar
        FolentraWindowWidthSizeClass.Medium -> AdaptiveNavigationType.NavigationRail
        FolentraWindowWidthSizeClass.Expanded -> AdaptiveNavigationType.PermanentDrawer
    }
