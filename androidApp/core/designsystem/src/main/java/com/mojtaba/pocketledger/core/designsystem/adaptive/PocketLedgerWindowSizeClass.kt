package com.mojtaba.pocketledger.core.designsystem.adaptive

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class PocketLedgerWindowWidthSizeClass {
    Compact,
    Medium,
    Expanded,
}

fun pocketLedgerWindowWidthSizeClass(width: Dp): PocketLedgerWindowWidthSizeClass =
    when {
        width < MediumWindowWidthBreakpoint -> PocketLedgerWindowWidthSizeClass.Compact
        width < ExpandedWindowWidthBreakpoint -> PocketLedgerWindowWidthSizeClass.Medium
        else -> PocketLedgerWindowWidthSizeClass.Expanded
    }

val MediumWindowWidthBreakpoint: Dp = 600.dp
val ExpandedWindowWidthBreakpoint: Dp = 840.dp
