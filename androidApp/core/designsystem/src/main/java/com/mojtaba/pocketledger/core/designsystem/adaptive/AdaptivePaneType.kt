package com.mojtaba.pocketledger.core.designsystem.adaptive

enum class AdaptivePaneType {
    SinglePane,
    ListDetail,
}

fun adaptivePaneType(
    widthSizeClass: PocketLedgerWindowWidthSizeClass,
): AdaptivePaneType =
    if (supportsListDetail(widthSizeClass)) {
        AdaptivePaneType.ListDetail
    } else {
        AdaptivePaneType.SinglePane
    }

fun supportsListDetail(
    widthSizeClass: PocketLedgerWindowWidthSizeClass,
): Boolean =
    when (widthSizeClass) {
        PocketLedgerWindowWidthSizeClass.Compact -> false
        PocketLedgerWindowWidthSizeClass.Medium,
        PocketLedgerWindowWidthSizeClass.Expanded,
        -> true
    }
