package com.mojtaba.pocketledger.feature.search.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.mojtaba.pocketledger.core.data.search.SearchQuery
import com.mojtaba.pocketledger.core.data.search.SearchTransactionType
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import com.mojtaba.pocketledger.feature.search.presentation.preview.SearchPreviewFixtures
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchFieldRendersAndKeywordEntryEmitsAction() {
        val actions = mutableListOf<SearchAction>()
        setContent(SearchUiState(isLoading = false), actions::add)

        composeRule.onNodeWithContentDescription("Search transactions by keyword")
            .assertIsDisplayed()
            .performTextInput("coffee")

        assertEquals(SearchAction.KeywordChanged("coffee"), actions.single())
    }

    @Test
    fun typeFilterChipToggles() {
        val actions = mutableListOf<SearchAction>()
        setContent(SearchUiState(isLoading = false), actions::add)

        composeRule.onNodeWithText("Expense").performClick()

        assertEquals(SearchAction.TypeFilterChanged(SearchTransactionType.Expense), actions.single())
    }

    @Test
    fun clearFiltersActionResetsVisibleStateThroughActionBoundary() {
        val actions = mutableListOf<SearchAction>()
        setContent(
            SearchPreviewFixtures.contentState.copy(
                query = SearchQuery(text = "coffee"),
                keywordInput = "coffee",
            ),
            actions::add,
        )

        composeRule.onNodeWithContentDescription("Clear search filters").performClick()

        assertEquals(SearchAction.ClearFiltersClicked, actions.single())
    }

    @Test
    fun resultListRendersRowsAndClickEmitsAction() {
        val actions = mutableListOf<SearchAction>()
        setContent(SearchPreviewFixtures.contentState, actions::add)

        composeRule.onNodeWithText("Coffee Shop").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("-${'$'}4.50").assertIsDisplayed()
        composeRule.onNodeWithText("Work").assertIsDisplayed()

        assertEquals(SearchAction.ResultClicked("preview-transaction"), actions.single())
    }

    @Test
    fun emptyLedgerStateRenders() {
        setContent(SearchUiState(isLoading = false, isEmptyLedger = true))

        composeRule.onNodeWithText("No transactions yet").assertIsDisplayed()
        composeRule.onNodeWithText("Saved transactions will appear in search.").assertIsDisplayed()
    }

    @Test
    fun noResultsStateRenders() {
        setContent(SearchUiState(isLoading = false))

        composeRule.onNodeWithText("No matching transactions").assertIsDisplayed()
        composeRule.onNodeWithText("Clear filters or try a different keyword.").assertIsDisplayed()
    }

    @Test
    fun loadingAndErrorStatesRender() {
        setContent(SearchUiState(isLoading = true))
        composeRule.onNodeWithText("Searching transactions").assertIsDisplayed()

        setContent(SearchUiState(isLoading = false, errorMessage = "Local ledger unavailable"))
        composeRule.onNodeWithText("Could not search transactions").assertIsDisplayed()
        composeRule.onNodeWithText("Local ledger unavailable").assertIsDisplayed()
    }

    private fun setContent(
        uiState: SearchUiState,
        onAction: (SearchAction) -> Unit = {},
    ) {
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
                SearchScreen(
                    uiState = uiState,
                    onAction = onAction,
                )
            }
        }
    }
}
