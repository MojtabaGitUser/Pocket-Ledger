package com.mojtaba.pocketledger.feature.transaction.presentation.detail

sealed interface TransactionDetailEvent {
    data class EditTransaction(val transactionId: String) : TransactionDetailEvent
    data object NavigateBackAfterDelete : TransactionDetailEvent
    data object ShowDeleteUndoSnackbar : TransactionDetailEvent
    data class ShowDeleteFailedSnackbar(val message: String) : TransactionDetailEvent
}
