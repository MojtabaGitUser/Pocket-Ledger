package com.mojtaba.folentra.feature.search.presentation

import androidx.compose.runtime.Immutable
import com.mojtaba.folentra.core.designsystem.component.AmountDisplay

@Immutable
data class SearchResultUiModel(
    val transactionId: String,
    val title: String,
    val amount: AmountDisplay,
    val typeLabel: String,
    val categoryLabel: String,
    val dateLabel: String,
    val notePreview: String?,
    val tagLabels: List<String>,
    val contentDescription: String,
)
