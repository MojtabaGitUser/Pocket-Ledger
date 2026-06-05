package com.mojtaba.pocketledger.feature.dashboard.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import com.mojtaba.pocketledger.feature.dashboard.presentation.preview.DashboardPreviewFixtures
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingStateRendersMessage() {
        setContent(DashboardUiState.Loading)

        composeRule.onNodeWithText("Loading dashboard").assertIsDisplayed()
    }

    @Test
    fun contentStateRendersCashFlowSummary() {
        setContent(DashboardUiState.Content(DashboardPreviewFixtures.summary))

        composeRule.onNodeWithText("Income").assertIsDisplayed()
        composeRule.onNodeWithText("\$4,850.00").assertIsDisplayed()
        composeRule.onNodeWithText("Expenses").assertIsDisplayed()
        composeRule.onNodeWithText("\$2,318.45").assertIsDisplayed()
        composeRule.onNodeWithText("Net").assertIsDisplayed()
        composeRule.onNodeWithText("+\$2,531.55").assertIsDisplayed()
    }

    @Test
    fun emptyStateRendersMessage() {
        setContent(DashboardUiState.Empty)

        composeRule.onNodeWithText("No dashboard data yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add transactions and budgets to see cash flow, spending, and insights.")
            .assertIsDisplayed()
    }

    @Test
    fun contentStateRendersCategoryAndBudgetProgress() {
        setContent(DashboardUiState.Content(DashboardPreviewFixtures.summary))

        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Top spending"))
        composeRule.onNodeWithText("Top spending").assertIsDisplayed()
        composeRule.onNodeWithText("Housing").assertIsDisplayed()
        composeRule.onNodeWithText("\$1,250.00 (54%)").assertIsDisplayed()
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Budgets"))
        composeRule.onNodeWithText("Budgets").assertIsDisplayed()
        composeRule.onNodeWithText("Food budget").assertIsDisplayed()
        composeRule.onNodeWithText("Near limit").assertIsDisplayed()
    }

    @Test
    fun contentStateRendersInsightsAndRecentTransactions() {
        setContent(DashboardUiState.Content(DashboardPreviewFixtures.summary))

        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Insights"))
        composeRule.onNodeWithText("Insights").assertIsDisplayed()
        composeRule.onNodeWithText("Positive cash flow").assertIsDisplayed()
        composeRule.onNodeWithText("Net cash flow is +\$2,531.55.").assertIsDisplayed()
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Recent transactions"))
        composeRule.onNodeWithText("Recent transactions").assertIsDisplayed()
        composeRule.onNodeWithText("Salary").assertIsDisplayed()
        composeRule.onNodeWithText("Monthly paycheck", substring = true).assertIsDisplayed()
    }

    @Test
    fun contentStateWithEmptyListsRendersFallbacks() {
        setContent(DashboardUiState.Content(DashboardPreviewFixtures.emptySummary))

        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("No category spend"))
        composeRule.onNodeWithText("No category spend").assertIsDisplayed()
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("No active budgets"))
        composeRule.onNodeWithText("No active budgets").assertIsDisplayed()
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("No recent transactions"))
        composeRule.onNodeWithText("No recent transactions").assertIsDisplayed()
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("No data yet"))
        composeRule.onNodeWithText("No data yet").assertIsDisplayed()
    }

    @Test
    fun errorStateRendersRetryableMessage() {
        setContent(DashboardUiState.Error("Could not read local summaries."))

        composeRule.onNodeWithText("Could not load dashboard").assertIsDisplayed()
        composeRule.onNodeWithText("Could not read local summaries.").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun errorStateRetryInvokesAction() {
        var retryClicks = 0
        setContent(
            uiState = DashboardUiState.Error("Could not read local summaries."),
            onAction = { action ->
                if (action == DashboardAction.RetryClicked) {
                    retryClicks += 1
                }
            },
        )

        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(1, retryClicks)
    }

    @Test
    fun routeRendersDefaultEmptyState() {
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
                DashboardRoute()
            }
        }

        composeRule.onNodeWithText("No dashboard data yet").assertIsDisplayed()
    }

    private fun setContent(uiState: DashboardUiState) {
        setContent(uiState = uiState, onAction = {})
    }

    private fun setContent(
        uiState: DashboardUiState,
        onAction: (DashboardAction) -> Unit,
    ) {
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
                DashboardScreen(
                    uiState = uiState,
                    onAction = onAction,
                )
            }
        }
    }
}
