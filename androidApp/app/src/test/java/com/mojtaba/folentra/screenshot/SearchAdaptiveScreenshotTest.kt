package com.mojtaba.folentra.screenshot

import com.mojtaba.folentra.feature.search.presentation.SearchScreen
import com.mojtaba.folentra.feature.search.presentation.SearchUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SearchAdaptiveScreenshotTest(
    private val device: AdaptiveScreenshotDevice,
) {
    @get:Rule
    val screenshotRule = AdaptiveScreenshotRule(device)

    @Test
    fun searchInitialState() {
        screenshotRule.snapshotScreen("search", "initial_state") {
            SearchScreen(
                uiState = ScreenshotTestData.initialSearchState,
                onAction = {},
            )
        }
    }

    @Test
    fun searchPopulatedResults() {
        screenshotRule.snapshotScreen("search", "populated_results") {
            SearchScreen(
                uiState = ScreenshotTestData.populatedSearchState,
                onAction = {},
            )
        }
    }

    @Test
    fun searchEmptyLedgerState() {
        screenshotRule.snapshotScreen("search", "empty_ledger") {
            SearchScreen(
                uiState = ScreenshotTestData.emptyLedgerSearchState,
                onAction = {},
            )
        }
    }

    @Test
    fun searchNoResultsState() {
        screenshotRule.snapshotScreen("search", "no_results") {
            SearchScreen(
                uiState = ScreenshotTestData.noResultsSearchState,
                onAction = {},
            )
        }
    }

    @Test
    fun searchErrorState() {
        screenshotRule.snapshotScreen("search", "error_state") {
            SearchScreen(
                uiState = SearchUiState(
                    keywordInput = "coffee",
                    errorMessage = "Search index is unavailable.",
                    isLoading = false,
                ),
                onAction = {},
            )
        }
    }

    @Test
    fun searchFiltersVisible() {
        screenshotRule.snapshotScreen("search", "filters_visible") {
            SearchScreen(
                uiState = ScreenshotTestData.filteredSearchState,
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
