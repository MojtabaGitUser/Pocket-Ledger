package com.mojtaba.pocketledger.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import com.mojtaba.pocketledger.core.database.testCategory
import com.mojtaba.pocketledger.core.database.testTransaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TransactionDaoTest {
    private lateinit var database: PocketLedgerDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PocketLedgerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = database.categoryDao()
        transactionDao = database.transactionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertGetUpdateDelete_roundTripsTransaction() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction())

        assertEquals(-1_250L, transactionDao.getById("transaction-1")?.amountMinor)

        transactionDao.update(testTransaction(amountMinor = -2_000))
        assertEquals(-2_000L, transactionDao.getById("transaction-1")?.amountMinor)

        transactionDao.deleteById("transaction-1")
        assertNull(transactionDao.getById("transaction-1"))
    }

    @Test
    fun observeRecentTransactions_emitsUpdatesInDeterministicOrder() = runTest {
        categoryDao.insert(testCategory())

        assertEquals(emptyList<String>(), transactionDao.observeRecentTransactions(limit = 10).first().map { it.id })

        transactionDao.insert(testTransaction(id = "older", occurredAt = 1_700_000_000_000))
        transactionDao.insert(testTransaction(id = "newer", occurredAt = 1_700_000_200_000))

        val observedIds = transactionDao.observeRecentTransactions(limit = 10).first().map { it.id }

        assertEquals(listOf("newer", "older"), observedIds)
    }

    @Test
    fun deletingCategory_nullsTransactionCategory() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction())

        categoryDao.deleteById("category-food")

        assertNull(transactionDao.getById("transaction-1")?.categoryId)
    }
}
