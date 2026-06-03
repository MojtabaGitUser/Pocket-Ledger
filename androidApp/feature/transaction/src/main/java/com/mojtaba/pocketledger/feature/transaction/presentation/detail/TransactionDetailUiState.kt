package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.compose.runtime.Immutable
import com.mojtaba.pocketledger.feature.transaction.model.TransactionDetailUiModel

sealed interface TransactionDetailUiState {
    data object Loading : TransactionDetailUiState
    data object NotFound : TransactionDetailUiState

    @Immutable
    data class Content(
        val transaction: TransactionDetailUiModel,
    ) : TransactionDetailUiState

    @Immutable
    data class Error(
        val message: String,
    ) : TransactionDetailUiState
}
