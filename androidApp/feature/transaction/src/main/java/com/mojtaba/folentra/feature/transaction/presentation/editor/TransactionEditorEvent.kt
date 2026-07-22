package com.mojtaba.folentra.feature.transaction.presentation.editor

sealed interface TransactionEditorEvent {
    data object SaveCompleted : TransactionEditorEvent
    data class ShowSnackbar(val message: String) : TransactionEditorEvent
}
