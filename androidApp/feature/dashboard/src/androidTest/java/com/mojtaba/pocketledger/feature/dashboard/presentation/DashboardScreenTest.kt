package com.mojtaba.pocketledger.feature.dashboard.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass
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
        composeRule.onNodeWithContentDescription("Loading dashboard")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Loading"))
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
        composeRule.onNodeWithText("Set budget").assertIsDisplayed()
    }

    @Test
    fun setBudgetActionsInvokeDashboardAction() {
        val actions = mutableListOf<DashboardAction>()
        setContent(
            uiState = DashboardUiState.Empty,
            onAction = actions::add,
        )

        composeRule.onNodeWithText("Set budget").performClick()

        assertEquals(listOf(DashboardAction.SetBudgetClicked), actions)
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
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Food budget"))
        composeRule.onNodeWithText("Food budget").assertIsDisplayed()
        composeRule.onAllNodesWithText("Near limit")[0].assertIsDisplayed()
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
        composeRule.onNodeWithText("Set budget").assertIsDisplayed()
    }

    @Test
    fun contentSetBudgetActionInvokesDashboardAction() {
        val actions = mutableListOf<DashboardAction>()
        setContent(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            onAction = actions::add,
        )

        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Add budget"))
        composeRule.onNodeWithText("Add budget").performClick()

        assertEquals(listOf(DashboardAction.SetBudgetClicked), actions)
    }

    @Test
    fun contentWithLongTextDoesNotCrash() {
        val longText = "Very long dashboard label ".repeat(8)
        val summary = DashboardPreviewFixtures.summary.copy(
            topCategories = DashboardPreviewFixtures.summary.topCategories.mapIndexed { index, category ->
                if (index == 0) category.copy(categoryName = longText) else category
            },
            budgetProgress = DashboardPreviewFixtures.summary.budgetProgress.mapIndexed { index, budget ->
                if (index == 0) budget.copy(budgetName = longText, categoryName = longText) else budget
            },
            recentTransactions = DashboardPreviewFixtures.summary.recentTransactions.mapIndexed { index, transaction ->
                if (index == 0) transaction.copy(categoryName = longText, notePreview = longText) else transaction
            },
        )

        setContent(DashboardUiState.Content(summary))

        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Recent transactions"))
        composeRule.onNodeWithText("Recent transactions").assertIsDisplayed()
    }

    @Test
    fun contentRendersInCompactWidth() {
        setContent(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            widthDp = 360,
            widthSizeClass = PocketLedgerWindowWidthSizeClass.Compact,
        )

        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Recent transactions"))
        composeRule.onNodeWithText("Recent transactions").assertIsDisplayed()
    }

    @Test
    fun contentRendersInMediumWidth() {
        setContent(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            widthDp = 720,
            widthSizeClass = PocketLedgerWindowWidthSizeClass.Medium,
        )

        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Food budget"))
        composeRule.onNodeWithText("Food budget").assertIsDisplayed()
    }

    @Test
    fun contentRendersInExpandedWidth() {
        setContent(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            widthDp = 960,
            widthSizeClass = PocketLedgerWindowWidthSizeClass.Expanded,
        )

        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithTag("DashboardContentList").performScrollToNode(hasText("Food budget"))
        composeRule.onNodeWithText("Food budget").assertIsDisplayed()
    }

    @Test
    fun errorStateRendersRetryableMessage() {
        setContent(DashboardUiState.Error("Could not read local summaries."))

        composeRule.onNodeWithText("Could not load dashboard").assertIsDisplayed()
        composeRule.onNodeWithText("Could not read local summaries.").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(
            "Could not load dashboard. Could not read local summaries.",
        ).assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Error"))
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
        onAction: (DashboardAction) -> Unit = {},
        widthDp: Int? = null,
        widthSizeClass: PocketLedgerWindowWidthSizeClass = PocketLedgerWindowWidthSizeClass.Compact,
    ) {
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
                if (widthDp == null) {
                    DashboardScreen(
                        uiState = uiState,
                        widthSizeClass = widthSizeClass,
                        onAction = onAction,
                    )
                } else {
                    Box(modifier = androidx.compose.ui.Modifier.width(widthDp.dp)) {
                        DashboardScreen(
                            uiState = uiState,
                            widthSizeClass = widthSizeClass,
                            onAction = onAction,
                        )
                    }
                }
            }
        }
    }
}
