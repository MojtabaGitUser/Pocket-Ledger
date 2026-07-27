package com.mojtaba.folentra.feature.search.presentation

sealed interface SearchEffect {
    data class OpenTransaction(val transactionId: String) : SearchEffect
}
