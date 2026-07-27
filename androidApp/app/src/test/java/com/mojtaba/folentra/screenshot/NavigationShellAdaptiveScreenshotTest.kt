package com.mojtaba.folentra.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mojtaba.folentra.navigation.AdaptiveNavigationItem
import com.mojtaba.folentra.navigation.AdaptiveNavigationScaffold
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class NavigationShellAdaptiveScreenshotTest(
    private val device: AdaptiveScreenshotDevice,
) {
    @get:Rule
    val screenshotRule = AdaptiveScreenshotRule(device)

    @Test
    fun adaptiveNavigationShell() {
        screenshotRule.snapshotScreen("navigation", "shell") {
            AdaptiveNavigationScaffold(
                navigationType = device.navigationState.navigationType,
                navigationItems = navigationItems,
                title = "Dashboard",
                showTopBar = true,
            ) { paddingValues: PaddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Adaptive shell content")
                }
            }
        }
    }

    companion object {
        private val navigationItems = listOf(
            AdaptiveNavigationItem(
                label = "Dashboard",
                shortLabel = "D",
                selected = true,
                contentDescription = "Dashboard navigation destination",
                onClick = {},
            ),
            AdaptiveNavigationItem(
                label = "Transactions",
                shortLabel = "T",
                selected = false,
                contentDescription = "Transactions navigation destination",
                onClick = {},
            ),
            AdaptiveNavigationItem(
                label = "Search",
                shortLabel = "S",
                selected = false,
                contentDescription = "Search navigation destination",
                onClick = {},
            ),
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun devices(): Collection<Array<Any>> =
            AdaptiveDeviceMatrix.All.map { arrayOf(it) }
    }
}
