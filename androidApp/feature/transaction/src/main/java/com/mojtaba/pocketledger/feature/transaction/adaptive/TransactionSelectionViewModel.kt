package com.mojtaba.pocketledger.feature.transaction.adaptive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptivePaneType
import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass
import com.mojtaba.pocketledger.core.designsystem.adaptive.adaptivePaneType
import kotlinx.coroutines.flow.StateFlow

class TransactionSelectionViewModel(
    private val savedStateHandle: SavedStateHandle,
    initialSelectedTransactionId: String? = null,
) : ViewModel() {
    val selectedTransactionId: StateFlow<String?> =
        savedStateHandle.getStateFlow(SelectedTransactionIdKey, initialSelectedTransactionId?.cleanId())

    fun selectTransaction(transactionId: String) {
        savedStateHandle[SelectedTransactionIdKey] = transactionId.cleanId()
    }

    fun clearSelection() {
        savedStateHandle[SelectedTransactionIdKey] = null
    }

    fun clearSelectionIfDeleted(visibleTransactionIds: Set<String>) {
        val selectedId = selectedTransactionId.value ?: return
        if (selectedId !in visibleTransactionIds) {
            clearSelection()
        }
    }

    private companion object {
        const val SelectedTransactionIdKey = "selectedTransactionId"
    }
}

fun transactionPaneType(
    widthSizeClass: PocketLedgerWindowWidthSizeClass,
): AdaptivePaneType = adaptivePaneType(widthSizeClass)

private fun String.cleanId(): String? = trim().takeIf { it.isNotEmpty() }
