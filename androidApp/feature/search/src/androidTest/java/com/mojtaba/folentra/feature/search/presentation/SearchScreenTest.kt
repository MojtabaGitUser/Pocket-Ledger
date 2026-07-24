package com.mojtaba.folentra.feature.search.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.mojtaba.folentra.core.data.search.SearchQuery
import com.mojtaba.folentra.core.data.search.SearchTransactionType
import com.mojtaba.folentra.core.designsystem.theme.FolentraTheme
import com.mojtaba.folentra.feature.search.presentation.preview.SearchPreviewFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchFieldRendersAndKeywordEntryEmitsAction() {
        val actions = mutableListOf<SearchAction>()
        setContent(SearchUiState(isLoading = false), actions::add)

        composeRule.onNodeWithContentDescription("Keyword search").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Search transactions by keyword")
            .assertIsDisplayed()
            .performTextInput("coffee")

        assertEquals(SearchAction.KeywordChanged("coffee"), actions.single())
    }

    @Test
    fun typeFilterChipToggles() {
        val actions = mutableListOf<SearchAction>()
        setContent(SearchUiState(isLoading = false), actions::add)

        composeRule.onNodeWithContentDescription("Filter by transaction type Expense")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Not selected"))
            .performClick()

        assertEquals(SearchAction.TypeFilterChanged(SearchTransactionType.Expense), actions.single())
    }

    @Test
    fun semanticModeIsHiddenWhenCapabilityIsNotVisible() {
        setContent(SearchUiState(isLoading = false))

        assertTrue(
            composeRule
                .onAllNodesWithContentDescription("Semantic search, coming soon")
                .fetchSemanticsNodes()
                .isEmpty(),
        )
    }

    @Test
    fun semanticModeRendersDisabledPlaceholderWhenCapabilityIsVisible() {
        setContent(
            SearchUiState(
                capabilities = SearchCapabilities(
                    semanticSearchVisible = true,
                    semanticSearchAvailable = false,
                ),
                isLoading = false,
            ),
        )

        composeRule.onNodeWithContentDescription("Semantic search, coming soon")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Disabled, coming soon"))
    }

    @Test
    fun selectedFilterChipExposesStateDescription() {
        setContent(
            SearchPreviewFixtures.contentState.copy(
                query = SearchPreviewFixtures.contentState.query.copy(
                    filters = SearchPreviewFixtures.contentState.query.filters.copy(
                        transactionTypes = setOf(SearchTransactionType.Expense),
                    ),
                ),
            ),
        )

        composeRule.onNodeWithContentDescription("Filter by transaction type Expense")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
    }

    @Test
    fun clearFiltersActionResetsVisibleStateThroughActionBoundary() {
        val harness = SearchScreenHarness(
            SearchPreviewFixtures.contentState.copy(
                query = SearchQuery(
                    text = "coffee",
                    filters = SearchPreviewFixtures.contentState.query.filters.copy(
                        transactionTypes = setOf(SearchTransactionType.Expense),
                    ),
                ),
                keywordInput = "coffee",
            ),
        )
        composeRule.setContent {
            FolentraTheme(dynamicColor = false) {
                SearchScreen(
                    uiState = harness.uiState,
                    onAction = harness::onAction,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Filter by transaction type Expense")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
        composeRule.onNodeWithContentDescription("Search filters")
            .performScrollToNode(hasContentDescription("Clear search filters"))
        composeRule.onNodeWithContentDescription("Clear search filters").performClick()

        composeRule.onNodeWithContentDescription("Filter by transaction type Expense")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Not selected"))
        assertEquals(SearchQuery(), harness.uiState.query)
        assertEquals("", harness.uiState.keywordInput)
    }

    @Test
    fun resultListRendersRowsAndClickEmitsAction() {
        val actions = mutableListOf<SearchAction>()
        setContent(SearchPreviewFixtures.contentState, actions::add)

        composeRule.onNodeWithText("Coffee Shop").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("-${'$'}4.50").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Tags Work", substring = true).assertIsDisplayed()

        assertEquals(SearchAction.ResultClicked("preview-transaction"), actions.single())
    }

    @Test
    fun emptyLedgerStateRenders() {
        setContent(SearchUiState(isLoading = false, isEmptyLedger = true))

        composeRule.onNodeWithText("No transactions yet").assertIsDisplayed()
        composeRule.onNodeWithText("Saved transactions will appear in search.").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("No transactions yet. Saved transactions will appear in search.")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Empty"))
    }

    @Test
    fun noResultsStateRenders() {
        setContent(SearchUiState(isLoading = false))

        composeRule.onNodeWithText("No matching transactions").assertIsDisplayed()
        composeRule.onNodeWithText("Clear filters or try a different keyword.").assertIsDisplayed()
    }

    @Test
    fun loadingStateRenders() {
        setContent(SearchUiState(isLoading = true))
        composeRule.onNodeWithText("Searching transactions").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Searching transactions")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Loading"))

    }

    @Test
    fun errorStateRenders() {
        setContent(SearchUiState(isLoading = false, errorMessage = "Local ledger unavailable"))
        composeRule.onNodeWithText("Could not search transactions").assertIsDisplayed()
        composeRule.onNodeWithText("Local ledger unavailable").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Could not search transactions. Local ledger unavailable")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Error"))
    }

    private fun setContent(
        uiState: SearchUiState,
        onAction: (SearchAction) -> Unit = {},
    ) {
        composeRule.setContent {
            FolentraTheme(dynamicColor = false) {
                SearchScreen(
                    uiState = uiState,
                    onAction = onAction,
                )
            }
        }
    }

    private class SearchScreenHarness(initialUiState: SearchUiState) {
        var uiState by mutableStateOf(initialUiState)

        fun onAction(action: SearchAction) {
            when (action) {
                is SearchAction.KeywordChanged -> updateQuery {
                    copy(text = action.text)
                }
                is SearchAction.SearchModeSelected -> updateQuery {
                    copy(mode = action.mode)
                }
                is SearchAction.TypeFilterChanged -> updateQuery {
                    copy(
                        filters = filters.copy(
                            transactionTypes = action.type?.let(::setOf).orEmpty(),
                        ),
                    )
                }
                SearchAction.ClearFiltersClicked -> uiState = uiState.copy(
                    query = SearchQuery(),
                    keywordInput = "",
                )
                is SearchAction.AmountRangeChanged,
                is SearchAction.CategoryToggled,
                is SearchAction.DateRangeChanged,
                is SearchAction.ResultClicked,
                is SearchAction.TagToggled,
                SearchAction.RetryClicked,
                -> Unit
            }
        }

        private fun updateQuery(reducer: SearchQuery.() -> SearchQuery) {
            val query = uiState.query.reducer()
            uiState = uiState.copy(
                query = query,
                keywordInput = query.text,
            )
        }
    }
}
