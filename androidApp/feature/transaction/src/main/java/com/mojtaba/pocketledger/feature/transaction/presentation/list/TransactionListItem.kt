package com.mojtaba.pocketledger.feature.transaction.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.mojtaba.pocketledger.core.designsystem.accessibility.pocketLedgerSelectedState
import com.mojtaba.pocketledger.core.designsystem.component.TransactionRow
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.transaction.model.TransactionListItemUiModel

@Composable
fun TransactionListItem(
    transaction: TransactionListItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    showDivider: Boolean = true,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        TransactionRow(
            title = transaction.title,
            amount = transaction.amount,
            modifier = Modifier
                .semantics {
                    this.selected = selected
                }
                .pocketLedgerSelectedState(selected),
            subtitle = listOfNotNull(transaction.typeLabel, transaction.dateLabel, transaction.notePreview)
                .joinToString(separator = " - "),
            category = transaction.categoryLabel,
            onClick = onClick,
            showDivider = showDivider && transaction.tagLabels.isEmpty(),
            onClickLabel = "Open transaction details",
            contentDescription = listOfNotNull(
                if (selected) "Selected transaction" else null,
                transaction.title,
                transaction.typeLabel,
                transaction.categoryLabel,
                transaction.dateLabel,
                transaction.notePreview,
                transaction.amount.contentDescription,
                transaction.tagLabels.takeIf { it.isNotEmpty() }?.joinToString(prefix = "Tags "),
            ).joinToString(separator = ", "),
        )
        if (transaction.tagLabels.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                contentPadding = PaddingValues(bottom = spacing.small),
                modifier = Modifier.padding(horizontal = spacing.medium),
            ) {
                items(transaction.tagLabels) { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag) },
                        modifier = Modifier.semantics {
                            contentDescription = "Tag $tag"
                        },
                    )
                }
            }
        }
    }
}
