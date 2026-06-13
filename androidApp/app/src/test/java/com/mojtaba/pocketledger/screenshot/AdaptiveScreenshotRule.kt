package com.mojtaba.pocketledger.screenshot

import androidx.compose.runtime.Composable
import app.cash.paparazzi.Paparazzi
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class AdaptiveScreenshotRule(
    private val device: AdaptiveScreenshotDevice,
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
        content: @Composable () -> Unit,
    ) {
        paparazzi.snapshot(name = "${group}_${device.id}_$name".asSnapshotName()) {
            PocketLedgerPreviewTheme {
                content()
            }
        }
    }

    private fun String.asSnapshotName(): String =
        replace(Regex("[^A-Za-z0-9_.-]"), "_")
}
