package com.mojtaba.pocketledger.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import com.mojtaba.pocketledger.core.data.search.SearchQuery
import com.mojtaba.pocketledger.core.data.search.SearchSort
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

        assertEquals(-1_250L, transactionRepository.getById("transaction-1")?.amountMinor)

        transactionRepository.update(testTransaction(amountMinor = -2_000))
        assertEquals(-2_000L, transactionRepository.getById("transaction-1")?.amountMinor)

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

    @Test
    fun searchTransactions_normalizesQueryBeforeSearching() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(testTransaction(id = "coffee", merchant = "Coffee Shop"))

        val observedIds = transactionRepository
            .searchTransactions(SearchQuery(text = "  coffee   "))
            .first()
            .map { it.id }

        assertEquals(listOf("coffee"), observedIds)
    }

    @Test
    fun searchTransactions_invalidQueryReturnsEmptyList() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(testTransaction(id = "coffee", merchant = "Coffee Shop"))

        val observedIds = transactionRepository
            .searchTransactions(SearchQuery(text = "a".repeat(SearchQuery.MAX_TEXT_LENGTH + 1)))
            .first()
            .map { it.id }

        assertEquals(emptyList<String>(), observedIds)
    }

    @Test
    fun searchTransactions_mapsResultsToLedgerTransactions() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(
            testTransaction(
                id = "coffee",
                amountMinor = -625,
                merchant = "Coffee Shop",
                note = "Latte",
            ),
        )

        val observed = transactionRepository
            .searchTransactions(SearchQuery(text = "coffee"))
            .first()
            .single()

        assertEquals("coffee", observed.id)
        assertEquals(-625, observed.amountMinor)
        assertEquals("Coffee Shop", observed.merchant)
        assertEquals("Latte", observed.note)
    }

    @Test
    fun searchTransactions_returnsMatchingTransactionAfterInsert() = runTest {
        categoryRepository.insert(testCategory())

        assertEquals(
            emptyList<String>(),
            transactionRepository.searchTransactions(SearchQuery(text = "coffee")).first().map { it.id },
        )

        transactionRepository.insert(testTransaction(id = "coffee", merchant = "Coffee Shop"))

        assertEquals(
            listOf("coffee"),
            transactionRepository.searchTransactions(SearchQuery(text = "coffee")).first().map { it.id },
        )
    }

    @Test
    fun searchTransactions_appliesSortOrder() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(
            testTransaction(
                id = "small",
                amountMinor = -100,
                merchant = "Coffee Shop",
                occurredAt = 1_700_000_000_000,
            ),
        )
        transactionRepository.insert(
            testTransaction(
                id = "large",
                amountMinor = -900,
                merchant = "Coffee Shop",
                occurredAt = 1_700_000_100_000,
            ),
        )

        val observedIds = transactionRepository
            .searchTransactions(
                SearchQuery(
                    text = "coffee",
                    sort = SearchSort.AmountAscending,
                ),
            )
            .first()
            .map { it.id }

        assertEquals(listOf("large", "small"), observedIds)
    }
}
