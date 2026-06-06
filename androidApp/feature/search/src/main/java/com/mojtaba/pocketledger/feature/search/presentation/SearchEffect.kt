package com.mojtaba.pocketledger.feature.search.presentation

sealed interface SearchEffect {
    data class OpenTransaction(val transactionId: String) : SearchEffect
}
