package com.mojtaba.folentra.adaptive

import androidx.compose.ui.unit.Dp
import com.mojtaba.folentra.core.designsystem.adaptive.AdaptiveNavigationState
import com.mojtaba.folentra.core.designsystem.adaptive.folentraWindowWidthSizeClass

fun adaptiveNavigationStateForWidth(width: Dp): AdaptiveNavigationState =
    AdaptiveNavigationState(
        widthSizeClass = folentraWindowWidthSizeClass(width),
    )
