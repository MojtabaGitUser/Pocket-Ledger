package com.mojtaba.folentra.feature.transaction.presentation.list

sealed interface TransactionListEvent {
    data class OpenDetail(val transactionId: String) : TransactionListEvent
}
