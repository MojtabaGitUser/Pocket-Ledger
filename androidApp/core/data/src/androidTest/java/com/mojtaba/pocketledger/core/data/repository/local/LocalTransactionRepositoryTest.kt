package com.mojtaba.pocketledger.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LocalTransactionRepositoryTest {
    private lateinit var database: PocketLedgerDatabase
    private lateinit var categoryRepository: LocalCategoryRepository
    private lateinit var transactionRepository: LocalTransactionRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PocketLedgerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryRepository = LocalCategoryRepository(database.categoryDao())
        transactionRepository = LocalTransactionRepository(database.transactionDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertGetUpdateDelete_roundTripsTransaction() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(testTransaction())

        assertEquals(-1_250, transactionRepository.getById("transaction-1")?.amountMinor)

        transactionRepository.update(testTransaction(amountMinor = -2_000))
        assertEquals(-2_000, transactionRepository.getById("transaction-1")?.amountMinor)

        val deleted = transactionRepository.deleteById("transaction-1")

        assertEquals(true, deleted)
        assertNull(transactionRepository.getById("transaction-1"))
    }

    @Test
    fun observeRecentTransactions_emitsLocalDatabaseUpdatesInOrder() = runTest {
        categoryRepository.insert(testCategory())

        assertEquals(emptyList<String>(), transactionRepository.observeRecentTransactions(limit = 10).first().map { it.id })

        transactionRepository.insert(testTransaction(id = "older", occurredAt = 1_700_000_000_000))
        transactionRepository.insert(testTransaction(id = "newer", occurredAt = 1_700_000_200_000))

        val observedIds = transactionRepository.observeRecentTransactions(limit = 10).first().map { it.id }

        assertEquals(listOf("newer", "older"), observedIds)
    }

    @Test
    fun observeTransactionsByCategory_filtersByCategory() = runTest {
        categoryRepository.insert(testCategory(id = "food"))
        categoryRepository.insert(testCategory(id = "travel", name = "Travel"))
        transactionRepository.insert(testTransaction(id = "food-1", categoryId = "food"))
        transactionRepository.insert(testTransaction(id = "travel-1", categoryId = "travel"))

        val observedIds = transactionRepository.observeTransactionsByCategory("food").first().map { it.id }

        assertEquals(listOf("food-1"), observedIds)
    }
}
