package com.mojtaba.folentra.feature.transaction.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.mojtaba.folentra.core.designsystem.accessibility.folentraSelectedState
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults
import com.mojtaba.folentra.feature.transaction.presentation.editor.TransactionTagOption

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    tags: List<TransactionTagOption>,
    selectedTagIds: Set<String>,
    onTagToggled: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text("Tags")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
            modifier = Modifier.fillMaxWidth(),
        ) {
            tags.forEach { tag ->
                val selected = tag.id in selectedTagIds
                FilterChip(
                    selected = selected,
                    onClick = { onTagToggled(tag.id) },
                    label = { Text(tag.name) },
                    modifier = Modifier.semantics {
                        contentDescription = "Tag ${tag.name}"
                        this.selected = selected
                    }.folentraSelectedState(selected),
                )
            }
        }
    }
}
