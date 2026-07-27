package com.mojtaba.folentra.screenshot

import androidx.compose.runtime.Composable
import app.cash.paparazzi.Paparazzi
import com.mojtaba.folentra.core.designsystem.theme.FolentraPreviewTheme
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

enum class ScreenshotTheme(
    val id: String,
    val isDark: Boolean,
) {
    Light(id = "light", isDark = false),
    Dark(id = "dark", isDark = true),
}

class AdaptiveScreenshotRule(
    private val device: AdaptiveScreenshotDevice,
    private val theme: ScreenshotTheme = ScreenshotTheme.Light,
) : TestRule {
    private val paparazzi = Paparazzi(
        deviceConfig = device.config,
        theme = "android:style/Theme.Material.Light.NoActionBar",
    )

    override fun apply(base: Statement, description: Description): Statement =
        paparazzi.apply(base, description)

    fun snapshotScreen(
        group: String,
        name: String,
        includeThemeInName: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        val themeSegment = if (includeThemeInName) "${theme.id}_" else ""
        paparazzi.snapshot(name = "${group}_${themeSegment}${device.id}_$name".asSnapshotName()) {
            FolentraPreviewTheme(darkTheme = theme.isDark) {
                content()
            }
        }
    }

    private fun String.asSnapshotName(): String =
        replace(Regex("[^A-Za-z0-9_.-]"), "_")
}
