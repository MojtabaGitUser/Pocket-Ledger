package com.mojtaba.folentra.feature.transaction.draft

import com.mojtaba.folentra.core.data.model.DraftTransaction
import com.mojtaba.folentra.core.data.repository.DraftTransactionRepository
import com.mojtaba.folentra.core.testing.coroutine.MainDispatcherRule
import com.mojtaba.folentra.core.testing.repository.FakeCategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import androidx.lifecycle.SavedStateHandle
import com.mojtaba.folentra.core.data.receipt.ReceiptTextRecognizer
import com.mojtaba.folentra.core.data.receipt.ReceiptText
import com.mojtaba.folentra.core.data.receipt.ReceiptLine
import kotlinx.coroutines.flow.first
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DraftTransactionsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private class Repository : DraftTransactionRepository {
        val rows = MutableStateFlow<List<DraftTransaction>>(emptyList())
        var confirmations = 0
        var fail = false
        override fun observePending() = rows
        override suspend fun save(draft: DraftTransaction) {
            if (fail) error("sensitive database failure")
            rows.value = rows.value.filterNot { it.id == draft.id } + draft
        }
        override suspend fun confirm(draft: DraftTransaction): String {
            if (fail) error("private merchant")
            confirmations++
            discard(draft.id)
            return "draft:${draft.id}"
        }
        override suspend fun discard(id: String) { rows.value = rows.value.filterNot { it.id == id } }
    }
    @Test fun incompleteDraftIsSavedButCannotBeConfirmed() = runTest {
        val repository = Repository()
        val model = DraftTransactionsViewModel(repository, FakeCategoryRepository(), { 1000 }, { "one" })
        advanceUntilIdle()
        model.create(); advanceUntilIdle()
        model.confirm(); advanceUntilIdle()
        assertEquals(0, repository.confirmations)
        model.edit { copy(merchant = "Cafe") }
        model.saveAndClose(); advanceUntilIdle()
        assertEquals("Cafe", repository.rows.value.single().merchant)
        assertNull(model.state.value.selected)
    }
    @Test fun repeatedClicksAreGuardedBeforeCoroutineStarts() = runTest {
        val repository = Repository()
        val model = DraftTransactionsViewModel(repository, FakeCategoryRepository(), { 1000 }, { "one" })
        advanceUntilIdle(); model.create(); advanceUntilIdle()
        model.edit { copy(type = "income", amountInput = "12.34") }
        model.confirm(); model.confirm(); advanceUntilIdle()
        assertEquals(1, repository.confirmations)
        assertNull(model.state.value.selected)
    }
    @Test fun failurePreservesInputAndAllowsRetryWithoutLeakingDetails() = runTest {
        val repository = Repository()
        val model = DraftTransactionsViewModel(repository, FakeCategoryRepository(), { 1000 }, { "one" })
        advanceUntilIdle(); model.create(); advanceUntilIdle()
        model.edit { copy(type = "income", amountInput = "20") }
        repository.fail = true
        model.confirm(); advanceUntilIdle()
        assertEquals("20", model.state.value.selected!!.amountInput)
        assertFalse(model.state.value.message!!.contains("private merchant"))
        assertFalse(model.state.value.busy)
        repository.fail = false
        model.confirm(); advanceUntilIdle()
        assertEquals(1, repository.confirmations)
    }

    private val scanTime = Instant.parse("2026-11-01T12:00:00Z").toEpochMilli()
    private val receipt = ReceiptText(listOf("MAPLE MARKET", "TOTAL CAD 12.50", "2026-10-26").map { ReceiptLine(it) })

    @Test fun scanningPopulatesOnlyDraftAndPreservesExistingCorrections() = runTest {
        val repository = Repository()
        val model = DraftTransactionsViewModel(repository, FakeCategoryRepository(), { scanTime }, { "scan" },
            ReceiptTextRecognizer { receipt })
        advanceUntilIdle(); model.prepareImage {}; advanceUntilIdle()
        assertNull(model.state.value.selected!!.occurredAt)
        assertEquals("", model.state.value.selected!!.currencyCode)
        model.edit { copy(merchant = "Corrected merchant") }; advanceUntilIdle()
        model.scan("image")
        model.state.first { !it.busy }
        assertEquals("12.50", model.state.value.selected!!.amountInput)
        assertEquals("Corrected merchant", model.state.value.selected!!.merchant)
        assertEquals("CAD", model.state.value.selected!!.currencyCode)
        assertEquals(0, repository.confirmations)
        assertEquals(1, repository.rows.value.size)
        model.confirm(); advanceUntilIdle() // Missing expense category remains invalid.
        assertEquals(0, repository.confirmations)
    }

    @Test fun ocrFailureAndEmptyTextKeepManualEntryAvailable() = runTest {
        for (recognizer in listOf(ReceiptTextRecognizer { error("secret receipt") }, ReceiptTextRecognizer { ReceiptText(emptyList()) })) {
            val repository = Repository()
            val model = DraftTransactionsViewModel(repository, FakeCategoryRepository(), { scanTime }, { "scan" }, recognizer)
            advanceUntilIdle(); model.prepareImage {}; advanceUntilIdle()
            model.edit { copy(amountInput = "7.00") }; advanceUntilIdle()
            model.scan("image"); model.state.first { !it.busy }
            assertEquals("7.00", model.state.value.selected!!.amountInput)
            assertNull(model.state.value.selected!!.occurredAt)
            assertFalse(model.state.value.message!!.contains("secret"))
            model.edit { copy(merchant = "Manual") }; advanceUntilIdle()
            assertEquals("Manual", model.state.value.selected!!.merchant)
            assertEquals(0, repository.confirmations)
        }
    }

    @Test fun savedStateRecreationKeepsCorrectionsWithoutConfirming() = runTest {
        val repository = Repository()
        val saved = SavedStateHandle()
        val model = DraftTransactionsViewModel(repository, FakeCategoryRepository(), { scanTime }, { "scan" }, saved = saved)
        advanceUntilIdle(); model.prepareImage {}; advanceUntilIdle()
        model.edit { copy(merchant = "Correction", amountInput = "3.21") }; advanceUntilIdle()
        val restored = DraftTransactionsViewModel(repository, FakeCategoryRepository(), { scanTime }, saved = SavedStateHandle(
            mapOf("draft" to saved.get<ArrayList<String>>("draft"), "scanning" to true)))
        advanceUntilIdle()
        assertEquals("Correction", restored.state.value.selected!!.merchant)
        assertEquals("3.21", restored.state.value.selected!!.amountInput)
        assertFalse(restored.state.value.busy)
        assertTrue(restored.state.value.message!!.contains("interrupted"))
        assertEquals(0, repository.confirmations)
    }

    @Test fun lateImageCannotPopulateAnotherDraftAndConfirmedStateIsNotReopened() = runTest {
        val repository = Repository()
        val saved = SavedStateHandle()
        val model = DraftTransactionsViewModel(repository, FakeCategoryRepository(), { scanTime }, { "one" },
            ReceiptTextRecognizer { receipt }, saved)
        advanceUntilIdle(); model.create(); advanceUntilIdle()
        model.scan("old-image", expectedDraftId = "different-draft"); advanceUntilIdle()
        assertEquals("", model.state.value.selected!!.amountInput)
        model.edit { copy(amountInput = "5.00", type = "income") }; advanceUntilIdle()
        val beforeConfirmation = ArrayList(saved.get<ArrayList<String>>("draft")!!)
        model.confirm(); advanceUntilIdle()
        val restored = DraftTransactionsViewModel(repository, FakeCategoryRepository(), { scanTime },
            saved = SavedStateHandle(mapOf("draft" to beforeConfirmation)))
        advanceUntilIdle()
        assertNull(restored.state.value.selected)
        assertEquals(1, repository.confirmations)
    }
}
