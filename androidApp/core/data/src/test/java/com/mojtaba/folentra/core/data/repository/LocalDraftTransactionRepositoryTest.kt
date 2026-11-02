package com.mojtaba.folentra.core.data.repository

import com.mojtaba.folentra.core.data.model.DraftTransaction
import com.mojtaba.folentra.core.data.repository.local.LocalDraftTransactionRepository
import com.mojtaba.folentra.core.database.dao.DraftTransactionDao
import com.mojtaba.folentra.core.database.model.DraftTransactionEntity
import com.mojtaba.folentra.core.database.model.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LocalDraftTransactionRepositoryTest {
    private class Dao : DraftTransactionDao() {
        var stored: DraftTransactionEntity? = null
        val ledger = mutableListOf<TransactionEntity>()
        override fun observePending() = MutableStateFlow(listOfNotNull(stored))
        override suspend fun getById(id: String) = stored?.takeIf { it.id == id }
        override suspend fun insert(draft: DraftTransactionEntity) { stored = draft }
        override suspend fun update(draft: DraftTransactionEntity) { stored = draft }
        override suspend fun discard(id: String): Int { stored = null; return 1 }
        override suspend fun insertTransaction(transaction: TransactionEntity) { ledger += transaction }
    }
    private fun draft() = DraftTransaction("one", createdAt = 100, updatedAt = 100)

    @Test fun incompleteInputCanBeSavedButNotConfirmed() = runBlocking {
        val dao = Dao()
        val repository = LocalDraftTransactionRepository(dao) { 1000 }
        repository.save(draft())
        assertNotNull(dao.stored)
        try { repository.confirm(draft()); fail("Invalid draft was confirmed") }
        catch (_: IllegalArgumentException) { }
        assertTrue(dao.ledger.isEmpty())
    }
    @Test fun expenseConfirmationUsesSharedNormalizationAndSignsAmount() = runBlocking {
        val dao = Dao()
        val repository = LocalDraftTransactionRepository(dao) { 1000 }
        val draft = draft().copy(amountInput = "12.34", currencyCode = " cad ",
            categoryId = "food", occurredAt = 100, merchant = " Cafe ")
        repository.save(draft)
        assertEquals("draft:one", repository.confirm(draft))
        val transaction = dao.ledger.single()
        assertEquals(-1234L, transaction.amountMinor)
        assertEquals("CAD", transaction.currencyCode)
        assertEquals("Cafe", transaction.merchant)
        assertEquals("receipt_manual", transaction.source)
        assertEquals("", dao.stored!!.merchant)
        repository.confirm(draft)
        assertEquals(1, dao.ledger.size)
    }
    @Test fun futureDateIsRejectedWithoutChangingPendingDraft() = runBlocking {
        val dao = Dao()
        val repository = LocalDraftTransactionRepository(dao) { 1000 }
        val draft = draft().copy(type = "income", amountInput = "1", occurredAt = Long.MAX_VALUE)
        repository.save(draft)
        try { repository.confirm(draft); fail("Future transaction was confirmed") }
        catch (_: IllegalArgumentException) { }
        assertNull(dao.stored!!.confirmedTransactionId)
        assertTrue(dao.ledger.isEmpty())
    }
}
