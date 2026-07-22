package com.mojtaba.folentra.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.mojtaba.folentra.core.designsystem.accessibility.folentraHeading
import com.mojtaba.folentra.core.designsystem.accessibility.folentraSelectedState
import com.mojtaba.folentra.core.designsystem.adaptive.AdaptiveNavigationType
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults

@Immutable
data class AdaptiveNavigationItem(
    val label: String,
    val shortLabel: String,
    val selected: Boolean,
    val contentDescription: String,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveNavigationScaffold(
    navigationType: AdaptiveNavigationType,
    navigationItems: List<AdaptiveNavigationItem>,
    title: String,
    showTopBar: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    when (navigationType) {
        AdaptiveNavigationType.BottomBar -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    if (showTopBar) {
                        AdaptiveTopAppBar(title = title)
                    }
                },
                bottomBar = {
                    AdaptiveBottomNavigationBar(items = navigationItems)
                },
                content = content,
            )
        }
        AdaptiveNavigationType.NavigationRail -> {
            Row(modifier = modifier.fillMaxSize()) {
                AdaptiveNavigationRail(items = navigationItems)
                Scaffold(
                    modifier = Modifier.weight(1f),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        if (showTopBar) {
                            AdaptiveTopAppBar(title = title)
                        }
                    },
                    content = content,
                )
            }
        }
        AdaptiveNavigationType.PermanentDrawer -> {
            PermanentNavigationDrawer(
                modifier = modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Permanent navigation drawer" },
                drawerContent = {
                    AdaptivePermanentDrawer(items = navigationItems)
                },
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        if (showTopBar) {
                            AdaptiveTopAppBar(title = title)
                        }
                    },
                    content = content,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdaptiveTopAppBar(title: String) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.folentraHeading(),
            )
        },
    )
}

@Composable
private fun AdaptiveBottomNavigationBar(
    items: List<AdaptiveNavigationItem>,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.semantics {
            contentDescription = "Bottom navigation"
        },
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = { DestinationIcon(label = item.shortLabel) },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                modifier = Modifier.semantics {
                    contentDescription = item.contentDescription
                }.folentraSelectedState(item.selected),
            )
        }
    }
}

@Composable
private fun AdaptiveNavigationRail(
    items: List<AdaptiveNavigationItem>,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing

    NavigationRail(
        modifier = modifier
            .padding(vertical = spacing.small)
            .semantics { contentDescription = "Navigation rail" },
    ) {
        items.forEach { item ->
            NavigationRailItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = { DestinationIcon(label = item.shortLabel) },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                modifier = Modifier.semantics {
                    contentDescription = item.contentDescription
                }.folentraSelectedState(item.selected),
            )
        }
    }
}

@Composable
private fun AdaptivePermanentDrawer(
    items: List<AdaptiveNavigationItem>,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing

    PermanentDrawerSheet(
        modifier = modifier.semantics {
            contentDescription = "Permanent navigation drawer"
        },
    ) {
        Box(modifier = Modifier.padding(top = spacing.medium))
        items.forEach { item ->
            NavigationDrawerItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = { DestinationIcon(label = item.shortLabel) },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .semantics { contentDescription = item.contentDescription }
                    .folentraSelectedState(item.selected),
            )
        }
    }
}

@Composable
private fun DestinationIcon(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = Modifier.clearAndSetSemantics {},
    )
}
