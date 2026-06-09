package com.mojtaba.pocketledger.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.mojtaba.pocketledger.PocketLedgerAppGraph
import com.mojtaba.pocketledger.adaptive.LocalAdaptiveNavigationState
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptiveNavigationState
import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import com.mojtaba.pocketledger.core.security.logging.AppLogger
import com.mojtaba.pocketledger.core.testing.fixture.testIncomeCategory
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerBudget
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerCategory
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerTag
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerTransaction
import com.mojtaba.pocketledger.core.testing.fixture.testTransactionTagLink
import com.mojtaba.pocketledger.core.testing.repository.FakeBudgetRepository
import com.mojtaba.pocketledger.core.testing.repository.FakeCategoryRepository
import com.mojtaba.pocketledger.core.testing.repository.FakeTagRepository
import com.mojtaba.pocketledger.core.testing.repository.FakeTransactionRepository
import com.mojtaba.pocketledger.core.testing.scheduler.FakeScheduler
import org.junit.Rule
import org.junit.Test

class PocketLedgerAppShellTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dashboardDestinationIsVisibleByDefault() {
        setContent()

        composeRule.onNodeWithText("No dashboard data yet").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Dashboard navigation destination")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
    }

    @Test
    fun selectingTransactionsDestinationShowsTransactionList() {
        setContent()

        composeRule.onNodeWithContentDescription("Transactions navigation destination").performClick()

        composeRule.waitUntilTextExists("Neighborhood Market")
        composeRule.onNodeWithText("Neighborhood Market").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Transactions navigation destination")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
    }

    @Test
    fun selectingSearchDestinationShowsSearchScreen() {
        setContent()

        composeRule.onNodeWithContentDescription("Search navigation destination").performClick()

        composeRule.waitUntilTextExists("Keyword")
        composeRule.onNodeWithContentDescription("Search transactions by keyword").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Search navigation destination")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
    }

    private fun setContent() {
        val appGraph = testAppGraph()
        val adaptiveNavigationState = AdaptiveNavigationState(PocketLedgerWindowWidthSizeClass.Compact)

        composeRule.setContent {
            val appState = rememberPocketLedgerAppState(
                navController = rememberNavController(),
                includeDebugDestinations = false,
            )
            PocketLedgerTheme(dynamicColor = false) {
                CompositionLocalProvider(LocalAdaptiveNavigationState provides adaptiveNavigationState) {
                    PocketLedgerAppShell(
                        appState = appState,
                        appGraph = appGraph,
                        adaptiveNavigationState = adaptiveNavigationState,
                    )
                }
            }
        }
    }

    private fun testAppGraph(): PocketLedgerAppGraph {
        val tag = testLedgerTag()
        val transaction = testLedgerTransaction()
        val tagLink = testTransactionTagLink(transactionId = transaction.id, tagId = tag.id)

        return PocketLedgerAppGraph.createForTesting(
            transactionRepository = FakeTransactionRepository(
                initialTransactions = listOf(transaction),
                initialTagLinks = listOf(tagLink),
            ),
            budgetRepository = FakeBudgetRepository(initialBudgets = listOf(testLedgerBudget())),
            categoryRepository = FakeCategoryRepository(
                initialCategories = listOf(testLedgerCategory(), testIncomeCategory()),
            ),
            tagRepository = FakeTagRepository(
                initialTags = listOf(tag),
                initialLinks = listOf(tagLink),
            ),
            backgroundTaskScheduler = FakeScheduler(),
            appLogger = NoOpAppLogger,
        )
    }

    private object NoOpAppLogger : AppLogger {
        override fun debug(message: String) = Unit

        override fun info(message: String) = Unit

        override fun warning(message: String) = Unit

        override fun error(
            throwable: Throwable?,
            message: String,
        ) = Unit
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.waitUntilTextExists(text: String) {
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
