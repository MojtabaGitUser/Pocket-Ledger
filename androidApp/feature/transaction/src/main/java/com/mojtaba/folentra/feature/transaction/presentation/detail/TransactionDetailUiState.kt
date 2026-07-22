package com.mojtaba.folentra.feature.transaction.presentation.detail

import androidx.compose.runtime.Immutable
import com.mojtaba.folentra.feature.transaction.model.TransactionDetailUiModel

sealed interface TransactionDetailUiState {
    data object Loading : TransactionDetailUiState
    data object NotFound : TransactionDetailUiState

    @Immutable
    data class Content(
        val transaction: TransactionDetailUiModel,
        val showDeleteConfirmation: Boolean = false,
        val isDeleting: Boolean = false,
    ) : TransactionDetailUiState

    @Immutable
    data class Error(
        val message: String,
    ) : TransactionDetailUiState
}
