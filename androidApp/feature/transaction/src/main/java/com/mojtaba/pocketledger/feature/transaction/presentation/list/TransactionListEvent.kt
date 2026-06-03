package com.mojtaba.pocketledger.feature.transaction.presentation.list

sealed interface TransactionListEvent {
    data class OpenDetail(val transactionId: String) : TransactionListEvent
}
