package com.mojtaba.pocketledger.feature.transaction.model

import androidx.compose.runtime.Immutable
import com.mojtaba.pocketledger.core.designsystem.component.AmountDisplay
import com.mojtaba.pocketledger.core.designsystem.component.AmountTone

@Immutable
data class TransactionListItemUiModel(
    val id: String,
    val amount: AmountDisplay,
    val typeLabel: String,
    val categoryLabel: String,
    val dateLabel: String,
    val title: String,
    val notePreview: String?,
    val tagLabels: List<String>,
    val tone: AmountTone,
)
