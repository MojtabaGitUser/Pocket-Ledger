package com.mojtaba.pocketledger.adaptive

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo

enum class FoldablePosture {
    Flat,
    HalfOpened,
    Book,
    Tabletop,
    Unknown,
}

@Immutable
data class FoldableUiState(
    val posture: FoldablePosture = FoldablePosture.Flat,
    val isSeparating: Boolean = false,
) {
    companion object {
        val Flat = FoldableUiState()
    }
}

internal enum class FoldableFeatureOrientation {
    Vertical,
    Horizontal,
    Unknown,
}

internal enum class FoldableFeatureState {
    Flat,
    HalfOpened,
    Unknown,
}

internal data class FoldableFeatureSnapshot(
    val orientation: FoldableFeatureOrientation,
    val state: FoldableFeatureState,
    val isSeparating: Boolean,
)

@Composable
fun rememberFoldableUiState(): State<FoldableUiState> {
    val context = LocalContext.current
    val activity = context.findActivity()
    return produceState(initialValue = FoldableUiState.Flat, key1 = activity) {
        if (activity == null) {
            value = FoldableUiState.Flat
            return@produceState
        }

        WindowInfoTracker.getOrCreate(activity)
            .windowLayoutInfo(activity)
            .collect { layoutInfo ->
                value = layoutInfo.toFoldableUiState()
            }
    }
}

internal fun WindowLayoutInfo.toFoldableUiState(): FoldableUiState {
    val foldingFeature = displayFeatures
        .filterIsInstance<FoldingFeature>()
        .firstOrNull()
        ?: return FoldableUiState.Flat

    return foldingFeature.toSnapshot().toFoldableUiState()
}

internal fun FoldableFeatureSnapshot.toFoldableUiState(): FoldableUiState {
    val posture = when (state) {
        FoldableFeatureState.Flat -> FoldablePosture.Flat
        FoldableFeatureState.HalfOpened -> when (orientation) {
            FoldableFeatureOrientation.Vertical -> FoldablePosture.Book
            FoldableFeatureOrientation.Horizontal -> FoldablePosture.Tabletop
            FoldableFeatureOrientation.Unknown -> FoldablePosture.HalfOpened
        }
        FoldableFeatureState.Unknown -> FoldablePosture.Unknown
    }
    return FoldableUiState(
        posture = posture,
        isSeparating = isSeparating,
    )
}

private fun FoldingFeature.toSnapshot(): FoldableFeatureSnapshot =
    FoldableFeatureSnapshot(
        orientation = when (orientation) {
            FoldingFeature.Orientation.VERTICAL -> FoldableFeatureOrientation.Vertical
            FoldingFeature.Orientation.HORIZONTAL -> FoldableFeatureOrientation.Horizontal
            else -> FoldableFeatureOrientation.Unknown
        },
        state = when (state) {
            FoldingFeature.State.FLAT -> FoldableFeatureState.Flat
            FoldingFeature.State.HALF_OPENED -> FoldableFeatureState.HalfOpened
            else -> FoldableFeatureState.Unknown
        },
        isSeparating = isSeparating,
    )

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
