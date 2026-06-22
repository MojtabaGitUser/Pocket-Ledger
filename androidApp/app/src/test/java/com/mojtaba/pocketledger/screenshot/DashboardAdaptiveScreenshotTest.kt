package com.mojtaba.pocketledger.screenshot

import com.mojtaba.pocketledger.feature.dashboard.presentation.DashboardUiState
import com.mojtaba.pocketledger.feature.dashboard.presentation.DashboardScreen
import com.mojtaba.pocketledger.feature.dashboard.presentation.preview.DashboardPreviewFixtures
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DashboardAdaptiveScreenshotTest(
    private val device: AdaptiveScreenshotDevice,
) {
    @get:Rule
    val screenshotRule = AdaptiveScreenshotRule(device)

    @Test
    fun dashboardContent() {
        screenshotRule.snapshotScreen("dashboard", "content") {
            DashboardScreen(
                uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
                widthSizeClass = device.widthSizeClass,
                onAction = {},
            )
        }
    }

    @Test
    fun dashboardEmptyState() {
        screenshotRule.snapshotScreen("dashboard", "empty_state") {
            DashboardScreen(
                uiState = DashboardUiState.Content(DashboardPreviewFixtures.emptySummary),
                widthSizeClass = device.widthSizeClass,
                onAction = {},
            )
        }
    }

    @Test
    fun dashboardErrorState() {
        screenshotRule.snapshotScreen("dashboard", "error_state") {
            DashboardScreen(
                uiState = DashboardUiState.Error("Could not read local dashboard summaries."),
                widthSizeClass = device.widthSizeClass,
                onAction = {},
            )
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun devices(): Collection<Array<Any>> =
            AdaptiveDeviceMatrix.All.map { arrayOf(it) }
    }
}
