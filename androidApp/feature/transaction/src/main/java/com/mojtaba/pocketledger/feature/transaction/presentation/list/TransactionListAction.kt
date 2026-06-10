package com.mojtaba.pocketledger.feature.transaction.presentation.list

sealed interface TransactionListAction {
    data class TransactionClicked(val transactionId: String) : TransactionListAction
    data object RetryClicked : TransactionListAction
}
