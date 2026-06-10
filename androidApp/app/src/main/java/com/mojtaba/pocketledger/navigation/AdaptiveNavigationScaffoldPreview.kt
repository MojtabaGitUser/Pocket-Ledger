package com.mojtaba.pocketledger.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptiveNavigationType
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme

@Preview(
    name = "Compact",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun AdaptiveNavigationScaffoldCompactPreview() {
    AdaptiveNavigationScaffoldPreviewContent(AdaptiveNavigationType.BottomBar)
}

@Preview(
    name = "Medium",
    showBackground = true,
    widthDp = 700,
    heightDp = 900,
)
@Composable
private fun AdaptiveNavigationScaffoldMediumPreview() {
    AdaptiveNavigationScaffoldPreviewContent(AdaptiveNavigationType.NavigationRail)
}

@Preview(
    name = "Expanded",
    showBackground = true,
    widthDp = 1100,
    heightDp = 900,
)
@Composable
private fun AdaptiveNavigationScaffoldExpandedPreview() {
    AdaptiveNavigationScaffoldPreviewContent(AdaptiveNavigationType.PermanentDrawer)
}

@Composable
private fun AdaptiveNavigationScaffoldPreviewContent(
    navigationType: AdaptiveNavigationType,
) {
    PocketLedgerPreviewTheme {
        AdaptiveNavigationScaffold(
            navigationType = navigationType,
            navigationItems = previewNavigationItems,
            title = "Pocket Ledger",
            showTopBar = true,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Adaptive content")
            }
        }
    }
}

private val previewNavigationItems = listOf(
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
    AdaptiveNavigationItem(
        label = "Settings",
        shortLabel = "S",
        selected = false,
        contentDescription = "Settings navigation destination",
        onClick = {},
    ),
)
