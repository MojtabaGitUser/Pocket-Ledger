package com.mojtaba.folentra.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.navigation.compose.rememberNavController
import com.mojtaba.folentra.FolentraAppGraph
import com.mojtaba.folentra.adaptive.LocalAdaptiveNavigationState
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.ai.AiProviderSelector
import com.mojtaba.folentra.core.ai.NoOpAiProvider
import com.mojtaba.folentra.core.ai.RuleBasedAiProvider
import com.mojtaba.folentra.core.designsystem.adaptive.AdaptiveNavigationState
import com.mojtaba.folentra.core.designsystem.adaptive.FolentraWindowWidthSizeClass
import com.mojtaba.folentra.core.designsystem.theme.FolentraTheme
import com.mojtaba.folentra.core.featureflags.DefaultFeatureFlags
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.folentra.core.security.applock.AppLockAuthenticationResult
import com.mojtaba.folentra.core.security.applock.AppLockAuthenticator
import com.mojtaba.folentra.core.security.applock.AppLockAvailability
import com.mojtaba.folentra.core.security.applock.AppLockManager
import com.mojtaba.folentra.core.security.logging.AppLogger
import com.mojtaba.folentra.core.security.preferences.InMemorySensitivePreferences
import com.mojtaba.folentra.core.testing.featureflags.FakeFeatureFlagProvider
import com.mojtaba.folentra.core.testing.fixture.testIncomeCategory
import com.mojtaba.folentra.core.testing.fixture.testLedgerBudget
import com.mojtaba.folentra.core.testing.fixture.testLedgerCategory
import com.mojtaba.folentra.core.testing.fixture.testLedgerTag
import com.mojtaba.folentra.core.testing.fixture.testLedgerTransaction
import com.mojtaba.folentra.core.testing.fixture.testTransactionTagLink
import com.mojtaba.folentra.core.testing.repository.FakeBudgetRepository
import com.mojtaba.folentra.core.testing.repository.FakeCategoryRepository
import com.mojtaba.folentra.core.testing.repository.FakeTagRepository
import com.mojtaba.folentra.core.testing.repository.FakeTransactionRepository
import com.mojtaba.folentra.core.testing.scheduler.FakeScheduler
import org.junit.Rule
import org.junit.Test

class FolentraAppShellTest {
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
    fun addTransactionFromListOpensCreateEditor() {
        setContent()

        composeRule.onNodeWithContentDescription("Transactions navigation destination").performClick()
        composeRule.waitUntilTextExists("Add transaction")
        composeRule.onNodeWithText("Add transaction").performClick()

        composeRule.waitUntilTextExists("Create Transaction")
        composeRule.onNodeWithText("Create Transaction").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Save transaction")
            .performScrollTo()
            .assertIsDisplayed()
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

    @Test
    fun selectingSettingsDestinationShowsSettingsScreen() {
        setContent()

        composeRule.onNodeWithContentDescription("Settings navigation destination").performClick()

        composeRule.waitUntilTextExists("Security and privacy")
        composeRule.onNodeWithText("App lock").assertIsDisplayed()
        composeRule.onNodeWithText("Background jobs").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Settings navigation destination")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
    }

    @Test
    fun settingsMonthlySummaryControlsUpdateState() {
        setContent()

        composeRule.onNodeWithContentDescription("Settings navigation destination").performClick()
        composeRule.waitUntilTextExists("Background jobs")
        composeRule.onNodeWithContentDescription("Monthly summary preparation switch")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Off"))
            .performClick()

        composeRule.onNodeWithContentDescription("Monthly summary preparation switch")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "On"))
        composeRule.onNodeWithText("18:00", useUnmergedTree = true).assertIsEnabled().performClick()
        composeRule.onNodeWithText("Scheduled around 18:00.", substring = true).assertIsDisplayed()

    }

    @Test
    fun settingsSwitchRowsExposeOneActionableSemanticsNode() {
        setContent()

        composeRule.onNodeWithContentDescription("Settings navigation destination").performClick()
        composeRule.waitUntilTextExists("Background jobs")

        listOf(
            "App lock switch",
            "Backup-ready profile switch",
            "Monthly summary preparation switch",
        ).forEach { description ->
            composeRule.onAllNodesWithContentDescription(description)
                .assertCountEquals(1)
            composeRule.onNodeWithContentDescription(description)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
        }
    }

    @Test
    fun debugHealthDestinationShowsSafeDiagnosticsWhenIncluded() {
        setContent(
            includeDebugDestinations = true,
            widthSizeClass = FolentraWindowWidthSizeClass.Expanded,
        )

        composeRule.onNodeWithContentDescription("Debug navigation destination").performClick()

        composeRule.waitUntilTextExists("Debug health")
        composeRule.onNodeWithText("Build").assertIsDisplayed()
        composeRule.onNodeWithTag("DebugHealthList").performScrollToNode(hasText("CI/CD"))
        composeRule.onNodeWithText("CI/CD").assertIsDisplayed()
        composeRule.onNodeWithTag("DebugHealthList").performScrollToNode(hasText("Firebase/App Distribution"))
        composeRule.onNodeWithText("Firebase/App Distribution").assertIsDisplayed()
        composeRule.onNodeWithTag("DebugHealthList").performScrollToNode(hasText("Release Safety"))
        composeRule.onNodeWithText("Release Safety").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Release diagnostics privacy")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Release diagnostics privacy: Release hidden"))
        composeRule.onAllNodesWithText("FIREBASE_SERVICE_ACCOUNT_JSON")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("FIREBASE_TESTER_GROUPS")
            .assertCountEquals(0)
    }

    private fun setContent(
        includeDebugDestinations: Boolean = false,
        widthSizeClass: FolentraWindowWidthSizeClass = FolentraWindowWidthSizeClass.Compact,
    ) {
        val appGraph = testAppGraph()
        val adaptiveNavigationState = AdaptiveNavigationState(widthSizeClass)

        composeRule.setContent {
            val appState = rememberFolentraAppState(
                navController = rememberNavController(),
                includeDebugDestinations = includeDebugDestinations,
            )
            FolentraTheme(dynamicColor = false) {
                CompositionLocalProvider(LocalAdaptiveNavigationState provides adaptiveNavigationState) {
                    FolentraAppShell(
                        appState = appState,
                        appGraph = appGraph,
                        adaptiveNavigationState = adaptiveNavigationState,
                    )
                }
            }
        }
    }

    private fun testAppGraph(): FolentraAppGraph {
        val tag = testLedgerTag()
        val transaction = testLedgerTransaction()
        val tagLink = testTransactionTagLink(transactionId = transaction.id, tagId = tag.id)

        val featureFlagProvider = FakeFeatureFlagProvider().apply {
            enable(DefaultFeatureFlags.BackgroundJobsEnabled)
        }
        val featureFlags = FeatureFlagEvaluator(featureFlagProvider)
        val aiProviderSelector = AiProviderSelector(
            providers = listOf(RuleBasedAiProvider, NoOpAiProvider),
            featureFlags = featureFlags,
        )
        val sensitivePreferences = InMemorySensitivePreferences()

        return FolentraAppGraph.createForTesting(
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
            featureFlags = featureFlags,
            aiProviderSelector = aiProviderSelector,
            aiFallbackStrategy = AiFallbackStrategy(aiProviderSelector),
            sensitivePreferences = sensitivePreferences,
            appLockManager = AppLockManager(
                preferences = sensitivePreferences,
                authenticator = AlwaysAvailableAuthenticator,
            ),
            backgroundTaskScheduler = FakeScheduler(),
            appLogger = NoOpAppLogger,
        )
    }

    private object AlwaysAvailableAuthenticator : AppLockAuthenticator {
        override fun availability(): AppLockAvailability = AppLockAvailability.Available

        override suspend fun authenticate(): AppLockAuthenticationResult = AppLockAuthenticationResult.Success
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
