package com.mojtaba.pocketledger.feature.transaction.presentation.detail

sealed interface TransactionDetailEvent {
    data class EditTransaction(val transactionId: String) : TransactionDetailEvent
    data class DeleteRequested(val transactionId: String) : TransactionDetailEvent
}
