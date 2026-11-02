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
}
