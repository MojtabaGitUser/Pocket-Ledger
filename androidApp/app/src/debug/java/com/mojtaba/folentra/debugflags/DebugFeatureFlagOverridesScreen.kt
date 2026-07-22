package com.mojtaba.folentra.debugflags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.mojtaba.folentra.FolentraAppGraph
import com.mojtaba.folentra.core.designsystem.accessibility.folentraHeading
import com.mojtaba.folentra.core.designsystem.component.AdaptiveContainer
import com.mojtaba.folentra.core.designsystem.component.SectionHeader
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults
import com.mojtaba.folentra.core.featureflags.BooleanFeatureFlag
import com.mojtaba.folentra.core.featureflags.DefaultFeatureFlags
import com.mojtaba.folentra.core.featureflags.FeatureFlagKey
import com.mojtaba.folentra.core.featureflags.FeatureFlagValue

@Composable
fun DebugFeatureFlagOverridesScreen(
    appGraph: FolentraAppGraph,
    modifier: Modifier = Modifier,
) {
    val provider = appGraph.featureFlagProvider
    var overrides by remember(provider) { mutableStateOf(provider.overridesSnapshot()) }
    val booleanFlags = remember { DefaultFeatureFlags.All.filterIsInstance<BooleanFeatureFlag>() }

    DebugFeatureFlagOverridesContent(
        flags = booleanFlags,
        overrides = overrides,
        valueOf = provider::valueOf,
        onSetOverride = { flag, enabled ->
            provider.setOverride(flag, enabled)
            overrides = provider.overridesSnapshot()
        },
        onClearOverride = { flag ->
            provider.clearOverride(flag)
            overrides = provider.overridesSnapshot()
        },
        onClearAll = {
            provider.clearAllOverrides()
            overrides = provider.overridesSnapshot()
        },
        modifier = modifier,
    )
}

@Composable
fun DebugFeatureFlagOverridesContent(
    flags: List<BooleanFeatureFlag>,
    overrides: Map<FeatureFlagKey, FeatureFlagValue>,
    valueOf: (BooleanFeatureFlag) -> Boolean,
    onSetOverride: (BooleanFeatureFlag, Boolean) -> Unit,
    onClearOverride: (BooleanFeatureFlag) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    val activeOverrideCount = overrides.size

    AdaptiveContainer(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            item {
                SectionHeader(
                    title = "Feature flags",
                    subtitle = "Debug-only overrides for local development and tester builds",
                    actionLabel = if (activeOverrideCount > 0) "Reset all" else null,
                    onActionClick = if (activeOverrideCount > 0) onClearAll else null,
                )
            }
            items(flags, key = { it.key.value }) { flag ->
                DebugFeatureFlagRow(
                    flag = flag,
                    currentValue = valueOf(flag),
                    overrideValue = overrides[flag.key] as? FeatureFlagValue.BooleanValue,
                    onSetOverride = { enabled -> onSetOverride(flag, enabled) },
                    onClearOverride = { onClearOverride(flag) },
                )
            }
        }
    }
}

@Composable
private fun DebugFeatureFlagRow(
    flag: BooleanFeatureFlag,
    currentValue: Boolean,
    overrideValue: FeatureFlagValue.BooleanValue?,
    onSetOverride: (Boolean) -> Unit,
    onClearOverride: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    val overrideText = overrideValue?.let { "Override: ${it.value}" } ?: "Using default: ${flag.defaultValue}"
    val stateText = if (currentValue) "Enabled" else "Disabled"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Feature flag ${flag.key.value}"
                stateDescription = "$stateText. $overrideText"
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                ) {
                    Text(
                        text = flag.key.value,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.folentraHeading(),
                    )
                    Text(
                        text = flag.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = currentValue,
                    onCheckedChange = onSetOverride,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$stateText - $overrideText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (overrideValue != null) {
                    TextButton(onClick = onClearOverride) {
                        Text(text = "Use default")
                    }
                }
            }
        }
    }
}