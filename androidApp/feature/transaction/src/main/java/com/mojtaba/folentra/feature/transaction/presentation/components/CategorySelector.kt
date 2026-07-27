package com.mojtaba.folentra.feature.transaction.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mojtaba.folentra.core.designsystem.accessibility.folentraValidationError
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.mojtaba.folentra.core.designsystem.accessibility.folentraSelectedState
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults
import com.mojtaba.folentra.feature.transaction.presentation.editor.TransactionCategoryOption

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(
    categories: List<TransactionCategoryOption>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    errorText: String?,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text("Category")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
            modifier = Modifier.fillMaxWidth(),
        ) {
            categories.forEach { category ->
                val selected = category.id == selectedCategoryId
                FilterChip(
                    selected = selected,
                    onClick = { onCategorySelected(if (selected) null else category.id) },
                    label = { Text(category.name) },
                    modifier = Modifier.semantics {
                        contentDescription = "Category ${category.name}"
                        this.selected = selected
                    }.folentraSelectedState(selected),
                )
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                modifier = Modifier.folentraValidationError(errorText),
            )
        }
    }
}
