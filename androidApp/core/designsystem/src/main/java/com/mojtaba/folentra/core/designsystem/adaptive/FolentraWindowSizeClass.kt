package com.mojtaba.folentra.core.designsystem.adaptive

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class FolentraWindowWidthSizeClass {
    Compact,
    Medium,
    Expanded,
}

fun folentraWindowWidthSizeClass(width: Dp): FolentraWindowWidthSizeClass =
    when {
        width < MediumWindowWidthBreakpoint -> FolentraWindowWidthSizeClass.Compact
        width < ExpandedWindowWidthBreakpoint -> FolentraWindowWidthSizeClass.Medium
        else -> FolentraWindowWidthSizeClass.Expanded
    }

val MediumWindowWidthBreakpoint: Dp = 600.dp
val ExpandedWindowWidthBreakpoint: Dp = 840.dp
