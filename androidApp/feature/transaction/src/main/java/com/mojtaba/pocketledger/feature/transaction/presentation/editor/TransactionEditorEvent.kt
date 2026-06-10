package com.mojtaba.pocketledger.feature.transaction.presentation.editor

sealed interface TransactionEditorEvent {
    data object SaveCompleted : TransactionEditorEvent
    data class ShowSnackbar(val message: String) : TransactionEditorEvent
}
