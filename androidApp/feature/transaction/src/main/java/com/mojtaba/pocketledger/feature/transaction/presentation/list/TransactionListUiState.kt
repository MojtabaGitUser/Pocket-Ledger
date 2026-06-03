package com.mojtaba.pocketledger.feature.transaction.presentation.list

import androidx.compose.runtime.Immutable
import com.mojtaba.pocketledger.feature.transaction.model.TransactionListItemUiModel

sealed interface TransactionListUiState {
    data object Loading : TransactionListUiState
    data object Empty : TransactionListUiState

    @Immutable
    data class Content(
        val transactions: List<TransactionListItemUiModel>,
    ) : TransactionListUiState

    @Immutable
    data class Error(
        val message: String,
    ) : TransactionListUiState
}
