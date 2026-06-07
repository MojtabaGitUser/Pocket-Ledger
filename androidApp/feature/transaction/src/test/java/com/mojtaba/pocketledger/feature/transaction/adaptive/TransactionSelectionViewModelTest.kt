package com.mojtaba.pocketledger.feature.transaction.adaptive

import androidx.lifecycle.SavedStateHandle
import com.mojtaba.pocketledger.core.designsystem.adaptive.AdaptivePaneType
import com.mojtaba.pocketledger.core.designsystem.adaptive.PocketLedgerWindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionSelectionViewModelTest {
    @Test
    fun selectsAndClearsTransaction() {
        val viewModel = TransactionSelectionViewModel(SavedStateHandle())

        viewModel.selectTransaction("transaction-1")
        assertEquals("transaction-1", viewModel.selectedTransactionId.value)

        viewModel.clearSelection()
        assertNull(viewModel.selectedTransactionId.value)
    }

    @Test
    fun restoresInitialSelection() {
        val viewModel = TransactionSelectionViewModel(
            savedStateHandle = SavedStateHandle(),
            initialSelectedTransactionId = "transaction-2",
        )

        assertEquals("transaction-2", viewModel.selectedTransactionId.value)
    }

    @Test
    fun clearsDeletedSelectionWhenSelectedItemIsNotVisible() {
        val viewModel = TransactionSelectionViewModel(
            savedStateHandle = SavedStateHandle(),
            initialSelectedTransactionId = "deleted",
        )

        viewModel.clearSelectionIfDeleted(setOf("transaction-1", "transaction-2"))

        assertNull(viewModel.selectedTransactionId.value)
    }

    @Test
    fun keepsSelectionWhenSelectedItemIsVisible() {
        val viewModel = TransactionSelectionViewModel(
            savedStateHandle = SavedStateHandle(),
            initialSelectedTransactionId = "transaction-1",
        )

        viewModel.clearSelectionIfDeleted(setOf("transaction-1", "transaction-2"))

        assertEquals("transaction-1", viewModel.selectedTransactionId.value)
    }

    @Test
    fun mapsCompactToSinglePaneAndLargerWidthsToListDetail() {
        assertEquals(
            AdaptivePaneType.SinglePane,
            transactionPaneType(PocketLedgerWindowWidthSizeClass.Compact),
        )
        assertEquals(
            AdaptivePaneType.ListDetail,
            transactionPaneType(PocketLedgerWindowWidthSizeClass.Medium),
        )
        assertEquals(
            AdaptivePaneType.ListDetail,
            transactionPaneType(PocketLedgerWindowWidthSizeClass.Expanded),
        )
    }
}
