package com.mojtaba.pocketledger.screenshot

import app.cash.paparazzi.DeviceConfig
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.android.resources.ScreenSize
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptiveNavigationState
import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass

data class AdaptiveScreenshotDevice(
    val id: String,
    val label: String,
    val config: DeviceConfig,
    val widthSizeClass: PocketLedgerWindowWidthSizeClass,
) {
    val navigationState: AdaptiveNavigationState =
        AdaptiveNavigationState(widthSizeClass = widthSizeClass)

    fun withFontScale(fontScale: Float): AdaptiveScreenshotDevice =
        copy(
            id = "${id}_font_${fontScale.toString().replace('.', '_')}",
            label = "$label font $fontScale",
            config = config.copy(fontScale = fontScale),
        )

    override fun toString(): String = id
}

object AdaptiveDeviceMatrix {
    val CompactPhone = AdaptiveScreenshotDevice(
        id = "compact_phone",
        label = "Pixel 5 portrait",
        config = DeviceConfig.PIXEL_5,
        widthSizeClass = PocketLedgerWindowWidthSizeClass.Compact,
    )

    val CompactPhoneLandscape = AdaptiveScreenshotDevice(
        id = "compact_phone_landscape",
        label = "Pixel 5 landscape",
        config = DeviceConfig.PIXEL_5.copy(orientation = ScreenOrientation.LANDSCAPE),
        widthSizeClass = PocketLedgerWindowWidthSizeClass.Medium,
    )

    val MediumTablet = AdaptiveScreenshotDevice(
        id = "medium_tablet",
        label = "Nexus 7 portrait",
        config = DeviceConfig.NEXUS_7,
        widthSizeClass = PocketLedgerWindowWidthSizeClass.Medium,
    )

    val ExpandedTablet = AdaptiveScreenshotDevice(
        id = "expanded_tablet",
        label = "Pixel Tablet landscape",
        config = DeviceConfig.PIXEL_TABLET,
        widthSizeClass = PocketLedgerWindowWidthSizeClass.Expanded,
    )

    val FoldableOpen = AdaptiveScreenshotDevice(
        id = "foldable_open",
        label = "Pixel Fold open",
        config = DeviceConfig.PIXEL_FOLD,
        widthSizeClass = PocketLedgerWindowWidthSizeClass.Expanded,
    )

    val FoldableClosed = AdaptiveScreenshotDevice(
        id = "foldable_closed",
        label = "Pixel Fold closed",
        config = DeviceConfig.PIXEL_9_PRO_FOLD.copy(
            screenWidth = 1080,
            screenHeight = 2424,
            density = Density.create(420),
            size = ScreenSize.NORMAL,
        ),
        widthSizeClass = PocketLedgerWindowWidthSizeClass.Compact,
    )

    val DesktopWindow = AdaptiveScreenshotDevice(
        id = "desktop_window",
        label = "Desktop freeform 1440x1000",
        config = DeviceConfig.PIXEL_TABLET.copy(
            screenWidth = 1440,
            screenHeight = 1000,
            density = Density.MEDIUM,
            xdpi = 160,
            ydpi = 160,
            orientation = ScreenOrientation.LANDSCAPE,
            size = ScreenSize.XLARGE,
        ),
        widthSizeClass = PocketLedgerWindowWidthSizeClass.Expanded,
    )

    val All = listOf(
        CompactPhone,
        CompactPhoneLandscape,
        MediumTablet,
        ExpandedTablet,
        FoldableOpen,
        FoldableClosed,
        DesktopWindow,
    )

    val KeyFontScaleDevices = listOf(
        CompactPhone.withFontScale(1.3f),
        CompactPhone.withFontScale(1.5f),
        ExpandedTablet.withFontScale(1.3f),
        ExpandedTablet.withFontScale(1.5f),
    )

    val TwoHundredPercentFontScaleDevices = listOf(
        CompactPhone.withFontScale(2.0f),
        ExpandedTablet.withFontScale(2.0f),
    )
}
