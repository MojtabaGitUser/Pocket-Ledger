package com.mojtaba.pocketledger.core.designsystem.component

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.preview.PreviewCategories
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme

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
        },
        enabled = enabled,
    )
}

@Preview(showBackground = true)
@Composable
private fun CategoryChipPreview() {
    PocketLedgerPreviewTheme {
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
