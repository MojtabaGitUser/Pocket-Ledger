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
    fun searchTransactions_matchesMerchantPrefixCaseInsensitively() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "coffee", merchant = "Coffee Shop"))
        transactionDao.insert(testTransaction(id = "grocery", merchant = "Grocery Store"))

        val observedIds = transactionDao.searchTransactionsByDateDescending("coffee%").first().map { it.id }

        assertEquals(listOf("coffee"), observedIds)
    }

    @Test
    fun searchTransactions_matchesNotePrefix() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "note-match", note = "Latte with oat milk"))
        transactionDao.insert(testTransaction(id = "other", note = "Dinner"))

        val observedIds = transactionDao.searchTransactionsByDateDescending("latte%").first().map { it.id }

        assertEquals(listOf("note-match"), observedIds)
    }

    @Test
    fun searchTransactions_matchesSourcePrefix() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "manual-import", source = "manual import"))
        transactionDao.insert(testTransaction(id = "bank-sync", source = "bank sync"))

        val observedIds = transactionDao.searchTransactionsByDateDescending("manual%").first().map { it.id }

        assertEquals(listOf("manual-import"), observedIds)
    }

    @Test
    fun searchTransactions_blankKeywordReturnsAllInDateDescendingOrder() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "older", occurredAt = 1_700_000_000_000))
        transactionDao.insert(testTransaction(id = "newer", occurredAt = 1_700_000_200_000))

        val observedIds = transactionDao.searchTransactionsByDateDescending(null).first().map { it.id }

        assertEquals(listOf("newer", "older"), observedIds)
    }

    @Test
    fun searchTransactions_noMatchReturnsEmptyList() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "coffee", merchant = "Coffee Shop"))

        val observedIds = transactionDao.searchTransactionsByDateDescending("rent%").first().map { it.id }

        assertEquals(emptyList<String>(), observedIds)
    }

    @Test
    fun searchTransactions_escapedWildcardCharactersAreTreatedLiterally() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "literal-percent", merchant = "50% Store"))
        transactionDao.insert(testTransaction(id = "wildcard-candidate", merchant = "500 Store"))

        val observedIds = transactionDao.searchTransactionsByDateDescending("50\\%%").first().map { it.id }

        assertEquals(listOf("literal-percent"), observedIds)
    }

    @Test
    fun searchTransactions_doesNotSearchCategoryName() = runTest {
        categoryDao.insert(testCategory(id = "groceries", name = "Groceries"))
        transactionDao.insert(
            testTransaction(
                id = "transaction",
                categoryId = "groceries",
                merchant = "Market",
                note = "Weekly food",
                source = "manual",
            ),
        )

        val observedIds = transactionDao.searchTransactionsByDateDescending("groceries%").first().map { it.id }

        assertEquals(emptyList<String>(), observedIds)
    }

    @Test
    fun searchTransactions_supportsDateAscendingSort() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "older", occurredAt = 1_700_000_000_000))
        transactionDao.insert(testTransaction(id = "newer", occurredAt = 1_700_000_200_000))

        val observedIds = transactionDao.searchTransactionsByDateAscending(null).first().map { it.id }

        assertEquals(listOf("older", "newer"), observedIds)
    }

    @Test
    fun deletingCategory_nullsTransactionCategory() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction())

        categoryDao.deleteById("category-food")

        assertNull(transactionDao.getById("transaction-1")?.categoryId)
    }
}
