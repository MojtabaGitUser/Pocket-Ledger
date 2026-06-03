package com.mojtaba.pocketledger.feature.transaction.model

import androidx.compose.runtime.Immutable
import com.mojtaba.pocketledger.core.designsystem.component.AmountDisplay

@Immutable
data class TransactionDetailUiModel(
    val id: String,
    val amount: AmountDisplay,
    val typeLabel: String,
    val categoryLabel: String,
    val dateLabel: String,
    val merchantLabel: String?,
    val noteLabel: String?,
    val tagLabels: List<String>,
    val createdAtLabel: String,
    val updatedAtLabel: String,
)
