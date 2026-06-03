package com.mojtaba.pocketledger.feature.transaction.presentation.detail

sealed interface TransactionDetailAction {
    data object EditClicked : TransactionDetailAction
    data object DeleteClicked : TransactionDetailAction
    data object RetryClicked : TransactionDetailAction
}
