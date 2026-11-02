package com.mojtaba.folentra.core.database

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.mojtaba.folentra.core.database.model.DraftTransactionEntity
import com.mojtaba.folentra.core.database.model.TransactionEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.*

class DraftTransactionDatabaseTest {
    private fun draft() = DraftTransactionEntity("receipt-1", "12.34", "CAD", "income", 1000L,
        null, "Shop", "", "receipt_manual", 1000, 1000)
    private fun transaction() = TransactionEntity("draft:receipt-1", 1234, "CAD", "income", 1000,
        createdAt = 1000, updatedAt = 1000)

    @Test fun incompleteDraftPersistsWithoutEnteringLedger() = runTest {
        val file = File.createTempFile("folentra-draft", ".db").apply { delete() }
        try {
            createFolentraDatabase(file.path).let { db ->
                try {
                    db.draftTransactionDao().savePending(draft().copy(amountInput = "", occurredAt = null))
                    assertTrue(db.transactionDao().observeRecentTransactions(10).first().isEmpty())
                } finally { db.close() }
            }
            val reopened = createFolentraDatabase(file.path)
            try {
                assertEquals("", reopened.draftTransactionDao().observePending().first().single().amountInput)
            } finally { reopened.close() }
        } finally { file.delete() }
    }

    @Test fun concurrentConfirmationsAndRetryAfterLedgerDeletionDoNotDuplicate() = runTest {
        val file = File.createTempFile("folentra-confirm", ".db").apply { delete() }
        val db = createFolentraDatabase(file.path)
        try {
            val dao = db.draftTransactionDao()
            dao.savePending(draft())
            val results = List(8) { async { dao.confirm(draft(), transaction()) } }.awaitAll()
            assertEquals(setOf("draft:receipt-1"), results.toSet())
            assertEquals(1, db.transactionDao().observeRecentTransactions(10).first().size)
            assertTrue(dao.observePending().first().isEmpty())
            assertEquals("", dao.getById(draft().id)!!.merchant)
            assertEquals("", dao.getById(draft().id)!!.amountInput)
            assertFailsWith<IllegalStateException> { dao.savePending(draft()) }
            db.transactionDao().deleteById(transaction().id)
            assertEquals(transaction().id, dao.confirm(draft(), transaction()))
            assertTrue(db.transactionDao().observeRecentTransactions(10).first().isEmpty())
        } finally { db.close(); file.delete() }
    }

    @Test fun failedLedgerInsertRollsBackAndKeepsDraftEditable() = runTest {
        val file = File.createTempFile("folentra-rollback", ".db").apply { delete() }
        val db = createFolentraDatabase(file.path)
        try {
            val dao = db.draftTransactionDao()
            dao.savePending(draft())
            assertFails { dao.confirm(draft(), transaction().copy(categoryId = "missing-category")) }
            assertNull(dao.getById(draft().id)!!.confirmedTransactionId)
            assertEquals(1, dao.observePending().first().size)
            assertTrue(db.transactionDao().observeRecentTransactions(10).first().isEmpty())
            dao.discard(draft().id)
            assertFails { dao.confirm(draft(), transaction()) }
        } finally { db.close(); file.delete() }
    }

    @Test fun versionTwoUpgradePreservesLedgerAndCreatesDraftTable() = runTest {
        val file = File.createTempFile("folentra-migrate", ".db").apply { delete() }
        try {
            val initial = createFolentraDatabase(file.path)
            try { initial.transactionDao().insert(transaction()) } finally { initial.close() }
            // Reconstruct the released v2 schema: v3 only adds transaction_drafts.
            BundledSQLiteDriver().open(file.path).use { connection ->
                connection.execSQL("DROP TABLE transaction_drafts")
                connection.execSQL("PRAGMA user_version = 2")
                connection.execSQL("UPDATE room_master_table SET identity_hash = 'c2be33b45d7e3eaf109f7609d0186193' WHERE id = 42")
            }
            val upgraded = createFolentraDatabase(file.path)
            try {
                assertNotNull(upgraded.transactionDao().getById(transaction().id))
                upgraded.draftTransactionDao().savePending(draft())
                assertEquals(1, upgraded.draftTransactionDao().observePending().first().size)
            } finally { upgraded.close() }
        } finally { file.delete() }
    }
}
