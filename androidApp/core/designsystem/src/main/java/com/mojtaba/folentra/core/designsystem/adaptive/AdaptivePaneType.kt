package com.mojtaba.folentra.core.designsystem.adaptive

enum class AdaptivePaneType {
    SinglePane,
    ListDetail,
}

fun adaptivePaneType(
    widthSizeClass: FolentraWindowWidthSizeClass,
): AdaptivePaneType =
    if (supportsListDetail(widthSizeClass)) {
        AdaptivePaneType.ListDetail
    } else {
        AdaptivePaneType.SinglePane
    }

fun supportsListDetail(
    widthSizeClass: FolentraWindowWidthSizeClass,
): Boolean =
    when (widthSizeClass) {
        FolentraWindowWidthSizeClass.Compact -> false
        FolentraWindowWidthSizeClass.Medium,
        FolentraWindowWidthSizeClass.Expanded,
        -> true
    }
