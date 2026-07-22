package com.mojtaba.folentra.screenshot

import com.mojtaba.folentra.feature.dashboard.presentation.DashboardScreen
import com.mojtaba.folentra.feature.dashboard.presentation.DashboardUiState
import com.mojtaba.folentra.feature.dashboard.presentation.preview.DashboardPreviewFixtures
import com.mojtaba.folentra.feature.search.presentation.SearchScreen
import com.mojtaba.folentra.feature.transaction.adaptive.TransactionListDetailContent
import com.mojtaba.folentra.feature.transaction.presentation.detail.TransactionDetailContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FontScaleAdaptiveScreenshotTest(
    private val device: AdaptiveScreenshotDevice,
) {
    @get:Rule
    val screenshotRule = AdaptiveScreenshotRule(device)

    @Test
    fun dashboardLargeFont() {
        screenshotRule.snapshotScreen("accessibility/dashboard", "large_font") {
            DashboardScreen(
                uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
                widthSizeClass = device.widthSizeClass,
                onAction = {},
            )
        }
    }

    @Test
    fun transactionListDetailLargeFont() {
        screenshotRule.snapshotScreen("accessibility/transactions", "large_font") {
            TransactionListDetailContent(
                listUiState = ScreenshotTestData.transactionListContent,
                selectedTransactionId = ScreenshotTestData.selectedTransactionId,
                onListAction = {},
            ) {
                TransactionDetailContent(
                    uiState = ScreenshotTestData.transactionDetail,
                    onAction = {},
                )
            }
        }
    }

    @Test
    fun searchLargeFont() {
        screenshotRule.snapshotScreen("accessibility/search", "large_font") {
            SearchScreen(
                uiState = ScreenshotTestData.populatedSearchState,
                onAction = {},
            )
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun devices(): Collection<Array<Any>> =
            AdaptiveDeviceMatrix.KeyFontScaleDevices.map { arrayOf(it) }
    }
}
