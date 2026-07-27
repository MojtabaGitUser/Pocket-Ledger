package com.mojtaba.folentra.feature.dashboard.presentation.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mojtaba.folentra.core.designsystem.theme.FolentraTheme
import com.mojtaba.folentra.feature.dashboard.model.BudgetProgressStatus
import com.mojtaba.folentra.feature.dashboard.model.BudgetProgressSummary
import com.mojtaba.folentra.feature.dashboard.model.CashFlowSummary
import com.mojtaba.folentra.feature.dashboard.model.CategorySpendSummary
import com.mojtaba.folentra.feature.dashboard.model.DashboardInsight
import com.mojtaba.folentra.feature.dashboard.model.DashboardTransactionType
import com.mojtaba.folentra.feature.dashboard.model.RecentTransactionSummary
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardComponentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cashFlowSummaryCardRendersMetrics() {
        composeRule.setThemedContent {
            CashFlowSummaryCard(
                cashFlow = CashFlowSummary(
                    incomeMinor = 300_00,
                    expenseMinor = 125_50,
                    netMinor = 174_50,
                    currencyCode = "USD",
                ),
            )
        }

        composeRule.onNodeWithText("Cash flow").assertIsDisplayed()
        composeRule.onNodeWithText("Income").assertIsDisplayed()
        composeRule.onNodeWithText("\$300.00").assertIsDisplayed()
        composeRule.onNodeWithText("Expenses").assertIsDisplayed()
        composeRule.onNodeWithText("\$125.50").assertIsDisplayed()
        composeRule.onNodeWithText("Net").assertIsDisplayed()
        composeRule.onNodeWithText("+\$174.50").assertIsDisplayed()
    }

    @Test
    fun categorySpendChartRendersRows() {
        composeRule.setThemedContent {
            CategorySpendChart(
                categories = listOf(
                    CategorySpendSummary(
                        categoryId = "food",
                        categoryName = "Food",
                        amountMinor = 80_00,
                        currencyCode = "USD",
                        transactionCount = 4,
                        percentageOfExpense = 66.6,
                    ),
                ),
            )
        }

        composeRule.onNodeWithText("Top spending").assertIsDisplayed()
        composeRule.onNodeWithText("Food").assertIsDisplayed()
        composeRule.onNodeWithText("\$80.00 (67%)").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Food, \$80.00, 67% of expenses").assertExists()
        composeRule.onNodeWithContentDescription("67% of expenses")
            .assertExists()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "67% of expenses"))
    }

    @Test
    fun categorySpendChartRendersFallback() {
        composeRule.setThemedContent {
            CategorySpendChart(categories = emptyList())
        }

        composeRule.onNodeWithText("No category spend").assertIsDisplayed()
    }

    @Test
    fun budgetProgressCardRendersRowsAndAction() {
        var clicks = 0
        composeRule.setThemedContent {
            BudgetProgressCard(
                budgets = listOf(
                    BudgetProgressSummary(
                        budgetId = "food-budget",
                        budgetName = "Food budget",
                        categoryId = "food",
                        categoryName = "Food",
                        spentMinor = 90_00,
                        limitMinor = 100_00,
                        currencyCode = "USD",
                        progressPercent = 90.0,
                        status = BudgetProgressStatus.NearLimit,
                    ),
                ),
                onSetBudgetClick = { clicks += 1 },
            )
        }

        composeRule.onNodeWithText("Budgets").assertIsDisplayed()
        composeRule.onNodeWithText("Food budget").assertIsDisplayed()
        composeRule.onNodeWithText("Food - \$90.00 of \$100.00").assertIsDisplayed()
        composeRule.onNodeWithText("Near limit", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription("Food budget, \$90.00 spent of \$100.00, 90%, Near limit")
            .assertExists()
        composeRule.onNodeWithContentDescription("90% budget progress")
            .assertExists()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "90% budget progress, Near limit",
                ),
            )
        composeRule.onNodeWithText("Add budget").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun budgetProgressCardRendersFallbackAndAction() {
        var clicks = 0
        composeRule.setThemedContent {
            BudgetProgressCard(
                budgets = emptyList(),
                onSetBudgetClick = { clicks += 1 },
            )
        }

        composeRule.onNodeWithText("No active budgets").assertIsDisplayed()
        composeRule.onNodeWithText("Set budget").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun recentTransactionsCardRendersRowsAndLimitsToFive() {
        composeRule.setThemedContent {
            RecentTransactionsCard(
                transactions = (1..6).map { index ->
                    RecentTransactionSummary(
                        transactionId = "transaction-$index",
                        amountMinor = if (index == 1) 200_00 else -10_00,
                        currencyCode = "USD",
                        type = if (index == 1) DashboardTransactionType.Income else DashboardTransactionType.Expense,
                        categoryName = if (index == 1) "Salary" else "Food",
                        notePreview = "Note $index",
                        occurredAt = 1_700_000_000_000L,
                    )
                },
            )
        }

        composeRule.onNodeWithText("Recent transactions").assertIsDisplayed()
        composeRule.onNodeWithText("Salary").assertIsDisplayed()
        composeRule.onNodeWithText("+\$200.00").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Salary, Income, Nov 14 - Note 1, +\$200.00").assertExists()
        composeRule.onNodeWithText("Note 5", substring = true).assertExists()
        composeRule.onNodeWithText("Note 6", substring = true).assertDoesNotExist()
    }

    @Test
    fun recentTransactionsCardRendersFallback() {
        composeRule.setThemedContent {
            RecentTransactionsCard(transactions = emptyList())
        }

        composeRule.onNodeWithText("No recent transactions").assertIsDisplayed()
    }

    @Test
    fun dashboardInsightCardRendersInsights() {
        composeRule.setThemedContent {
            DashboardInsightCard(
                insights = listOf(
                    DashboardInsight.BudgetExceeded(
                        budgetId = "budget",
                        budgetName = "Food budget",
                        progressPercent = 120.0,
                    ),
                ),
            )
        }

        composeRule.onNodeWithText("Insights").assertIsDisplayed()
        composeRule.onNodeWithText("Budget exceeded").assertIsDisplayed()
        composeRule.onNodeWithText("Food budget is over budget at 120%.").assertIsDisplayed()
    }

    @Test
    fun dashboardInsightCardRendersFallback() {
        composeRule.setThemedContent {
            DashboardInsightCard(insights = emptyList())
        }

        composeRule.onNodeWithText("No data yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add transactions to start seeing dashboard insights.").assertIsDisplayed()
    }

    @Test
    fun dashboardEmptyStateRendersBudgetAction() {
        var clicks = 0
        composeRule.setThemedContent {
            DashboardEmptyState(onSetBudgetClick = { clicks += 1 })
        }

        composeRule.onNodeWithText("No dashboard data yet").assertIsDisplayed()
        composeRule.onNodeWithText("Set budget").performClick()

        assertEquals(1, clicks)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        setContent {
            FolentraTheme(dynamicColor = false) {
                content()
            }
        }
    }
}
