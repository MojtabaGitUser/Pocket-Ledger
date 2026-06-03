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
import com.mojtaba.pocketledger.core.designsystem.component.TransactionRow
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.transaction.model.TransactionListItemUiModel

@Composable
fun TransactionListItem(
    transaction: TransactionListItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        TransactionRow(
            title = transaction.title,
            amount = transaction.amount,
            subtitle = listOfNotNull(transaction.typeLabel, transaction.dateLabel, transaction.notePreview)
                .joinToString(separator = " - "),
            category = transaction.categoryLabel,
            onClick = onClick,
            showDivider = showDivider && transaction.tagLabels.isEmpty(),
            contentDescription = listOfNotNull(
                transaction.title,
                transaction.typeLabel,
                transaction.categoryLabel,
                transaction.dateLabel,
                transaction.notePreview,
                transaction.amount.contentDescription,
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
                    )
                }
            }
        }
    }
}
