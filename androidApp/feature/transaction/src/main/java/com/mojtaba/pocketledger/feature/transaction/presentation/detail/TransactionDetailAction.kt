package com.mojtaba.pocketledger.feature.transaction.presentation.detail

sealed interface TransactionDetailAction {
    data object EditClicked : TransactionDetailAction
    data object DeleteClicked : TransactionDetailAction
    data object DeleteConfirmed : TransactionDetailAction
    data object DeleteDismissed : TransactionDetailAction
    data object UndoDeleteClicked : TransactionDetailAction
    data object RetryClicked : TransactionDetailAction
}
