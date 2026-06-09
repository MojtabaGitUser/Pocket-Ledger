package com.mojtaba.pocketledger.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assert
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptiveNavigationType
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import org.junit.Rule
import org.junit.Test

class AdaptiveNavigationScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compactRendersBottomNavigation() {
        setContent(AdaptiveNavigationType.BottomBar)

        composeRule.onNodeWithContentDescription("Bottom navigation").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Dashboard navigation destination")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
    }

    @Test
    fun mediumRendersNavigationRail() {
        setContent(AdaptiveNavigationType.NavigationRail)

        composeRule.onNodeWithContentDescription("Navigation rail").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Dashboard navigation destination").assertIsSelected()
    }

    @Test
    fun expandedRendersPermanentDrawer() {
        setContent(AdaptiveNavigationType.PermanentDrawer)

        composeRule.onAllNodesWithContentDescription("Permanent navigation drawer")[0].assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Dashboard navigation destination").assertIsSelected()
    }

    @Test
    fun destinationSwitchingUpdatesSelectedDestination() {
        setContent(AdaptiveNavigationType.BottomBar)

        composeRule.onNodeWithContentDescription("Search navigation destination").performClick()

        composeRule.onNodeWithContentDescription("Search navigation destination")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
        composeRule.onNodeWithText("Selected Search").assertIsDisplayed()
    }

    private fun setContent(navigationType: AdaptiveNavigationType) {
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
                var selectedLabel by remember { mutableStateOf("Dashboard") }
                val items = listOf("Dashboard" to "D", "Transactions" to "T", "Search" to "S", "Settings" to "S")
                    .map { (label, shortLabel) ->
                        AdaptiveNavigationItem(
                            label = label,
                            shortLabel = shortLabel,
                            selected = label == selectedLabel,
                            contentDescription = "$label navigation destination",
                            onClick = { selectedLabel = label },
                        )
                    }

                AdaptiveNavigationScaffold(
                    navigationType = navigationType,
                    navigationItems = items,
                    title = selectedLabel,
                    showTopBar = true,
                ) {
                    Text(text = "Selected $selectedLabel")
                }
            }
        }
    }
}
