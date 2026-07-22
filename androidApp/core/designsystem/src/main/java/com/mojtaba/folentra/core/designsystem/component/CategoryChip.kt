package com.mojtaba.folentra.core.designsystem.component

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.folentra.core.designsystem.accessibility.folentraSelectedState
import com.mojtaba.folentra.core.designsystem.preview.PreviewCategories
import com.mojtaba.folentra.core.designsystem.theme.FolentraPreviewTheme

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String = label,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = label)
        },
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
            this.selected = selected
        }.folentraSelectedState(selected),
        enabled = enabled,
    )
}

@Preview(showBackground = true)
@Composable
private fun CategoryChipPreview() {
    FolentraPreviewTheme {
        androidx.compose.foundation.layout.Row {
            CategoryChip(
                label = PreviewCategories.food,
                selected = true,
                onClick = {},
            )
            CategoryChip(
                label = PreviewCategories.utilities,
                selected = false,
                onClick = {},
            )
        }
    }
}
