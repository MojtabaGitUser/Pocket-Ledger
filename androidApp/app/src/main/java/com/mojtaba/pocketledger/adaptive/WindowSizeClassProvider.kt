package com.mojtaba.pocketledger.adaptive

import androidx.compose.ui.unit.Dp
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptiveNavigationState
import com.mojtaba.pocketledger.core.designsystem.adaptive.pocketLedgerWindowWidthSizeClass

fun adaptiveNavigationStateForWidth(width: Dp): AdaptiveNavigationState =
    AdaptiveNavigationState(
        widthSizeClass = pocketLedgerWindowWidthSizeClass(width),
    )
