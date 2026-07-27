package com.mojtaba.folentra.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.database.FolentraDatabase
import com.mojtaba.folentra.core.database.testCategory
import com.mojtaba.folentra.core.database.testTag
import com.mojtaba.folentra.core.database.testTransaction
import com.mojtaba.folentra.core.database.testTransactionTagCrossRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDaoTest {
    private lateinit var database: FolentraDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var tagDao: TagDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FolentraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = database.categoryDao()
        tagDao = database.tagDao()
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
    fun observeById_emitsCreateUpdateDeleteChanges() = runTest {
        categoryDao.insert(testCategory())

        val emissions = mutableListOf<com.mojtaba.folentra.core.database.model.TransactionEntity?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            transactionDao.observeById("transaction-1")
                .take(4)
                .toList(emissions)
        }
        advanceUntilIdle()

        transactionDao.insert(testTransaction())
        advanceUntilIdle()
        transactionDao.update(testTransaction(amountMinor = -2_000))
        advanceUntilIdle()
        transactionDao.deleteById("transaction-1")
        advanceUntilIdle()

        assertEquals(listOf(null, -1_250L, -2_000L, null), emissions.map { it?.amountMinor })
    }

    @Test
    fun observeTransactionsByDateRange_returnsOnlyTransactionsInsideInclusiveRange() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "before", occurredAt = 1_699_999_999_999))
        transactionDao.insert(testTransaction(id = "start", occurredAt = 1_700_000_000_000))
        transactionDao.insert(testTransaction(id = "middle", occurredAt = 1_700_000_100_000))
        transactionDao.insert(testTransaction(id = "end", occurredAt = 1_700_000_200_000))
        transactionDao.insert(testTransaction(id = "after", occurredAt = 1_700_000_200_001))

        val observedIds = transactionDao.observeTransactionsByDateRange(
            startInclusive = 1_700_000_000_000,
            endInclusive = 1_700_000_200_000,
        ).first().map { it.id }

        assertEquals(listOf("end", "middle", "start"), observedIds)
    }

    @Test
    fun observeTransactionsByCategory_returnsOnlyMatchingCategory() = runTest {
        categoryDao.insert(testCategory(id = "food", name = "Food"))
        categoryDao.insert(testCategory(id = "transport", name = "Transport"))
        transactionDao.insert(testTransaction(id = "food-transaction", categoryId = "food"))
        transactionDao.insert(testTransaction(id = "transport-transaction", categoryId = "transport"))

        val observedIds = transactionDao.observeTransactionsByCategory("food").first().map { it.id }

        assertEquals(listOf("food-transaction"), observedIds)
    }

    @Test
    fun observeTransactionsByTag_emitsRelationshipChanges() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "tagged"))
        transactionDao.insert(testTransaction(id = "untagged"))
        tagDao.insert(testTag(id = "tag-essential", name = "Essential"))

        assertEquals(emptyList<String>(), transactionDao.observeTransactionsByTag("tag-essential").first().map { it.id })

        tagDao.addTagToTransaction(testTransactionTagCrossRef(transactionId = "tagged", tagId = "tag-essential"))
        assertEquals(listOf("tagged"), transactionDao.observeTransactionsByTag("tag-essential").first().map { it.id })

        tagDao.removeTagFromTransaction("tagged", "tag-essential")
        assertEquals(emptyList<String>(), transactionDao.observeTransactionsByTag("tag-essential").first().map { it.id })
    }

    @Test
    fun searchTransactions_matchesMerchantPrefixCaseInsensitively() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "coffee", merchant = "Coffee Shop"))
        transactionDao.insert(testTransaction(id = "grocery", merchant = "Grocery Store"))

        val observedIds = transactionDao.searchByDateDescending("coffee%").first().map { it.id }

        assertEquals(listOf("coffee"), observedIds)
    }

    @Test
    fun searchTransactions_matchesNotePrefix() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "note-match", note = "Latte with oat milk"))
        transactionDao.insert(testTransaction(id = "other", note = "Dinner"))

        val observedIds = transactionDao.searchByDateDescending("latte%").first().map { it.id }

        assertEquals(listOf("note-match"), observedIds)
    }

    @Test
    fun searchTransactions_matchesSourcePrefix() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "manual-import", source = "manual import"))
        transactionDao.insert(testTransaction(id = "csv-import", source = "csv import"))

        val observedIds = transactionDao.searchByDateDescending("manual%").first().map { it.id }

        assertEquals(listOf("manual-import"), observedIds)
    }

    @Test
    fun searchTransactions_blankKeywordReturnsAllInDateDescendingOrder() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "older", occurredAt = 1_700_000_000_000))
        transactionDao.insert(testTransaction(id = "newer", occurredAt = 1_700_000_200_000))

        val observedIds = transactionDao.searchByDateDescending(null).first().map { it.id }

        assertEquals(listOf("newer", "older"), observedIds)
    }

    @Test
    fun searchTransactions_noMatchReturnsEmptyList() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "coffee", merchant = "Coffee Shop"))

        val observedIds = transactionDao.searchByDateDescending("rent%").first().map { it.id }

        assertEquals(emptyList<String>(), observedIds)
    }

    @Test
    fun searchTransactions_escapedWildcardCharactersAreTreatedLiterally() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "literal-percent", merchant = "50% Store"))
        transactionDao.insert(testTransaction(id = "wildcard-candidate", merchant = "500 Store"))

        val observedIds = transactionDao.searchByDateDescending("50\\%%").first().map { it.id }

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

        val observedIds = transactionDao.searchByDateDescending("groceries%").first().map { it.id }

        assertEquals(emptyList<String>(), observedIds)
    }

    @Test
    fun searchTransactions_supportsDateAscendingSort() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction(id = "older", occurredAt = 1_700_000_000_000))
        transactionDao.insert(testTransaction(id = "newer", occurredAt = 1_700_000_200_000))

        val observedIds = transactionDao.searchByDateAscending(null).first().map { it.id }

        assertEquals(listOf("older", "newer"), observedIds)
    }

    @Test
    fun searchTransactions_appliesDashboardAndSearchFiltersTogether() = runTest {
        categoryDao.insert(testCategory(id = "food", name = "Food"))
        categoryDao.insert(testCategory(id = "transport", name = "Transport"))
        tagDao.insert(testTag(id = "tag-essential", name = "Essential"))
        tagDao.insert(testTag(id = "tag-weekend", name = "Weekend"))
        transactionDao.insert(
            testTransaction(
                id = "match",
                amountMinor = -1_500,
                occurredAt = 1_700_000_100_000,
                categoryId = "food",
                merchant = "Coffee Shop",
            ),
        )
        transactionDao.insert(
            testTransaction(
                id = "wrong-category",
                amountMinor = -1_500,
                occurredAt = 1_700_000_100_000,
                categoryId = "transport",
                merchant = "Coffee Shop",
            ),
        )
        transactionDao.insert(
            testTransaction(
                id = "wrong-amount",
                amountMinor = -50,
                occurredAt = 1_700_000_100_000,
                categoryId = "food",
                merchant = "Coffee Shop",
            ),
        )
        tagDao.addTagToTransaction(testTransactionTagCrossRef("match", "tag-essential"))
        tagDao.addTagToTransaction(testTransactionTagCrossRef("match", "tag-weekend"))
        tagDao.addTagToTransaction(testTransactionTagCrossRef("wrong-category", "tag-essential"))

        val observedIds = transactionDao.searchByDateDescending(
            keywordPattern = "coffee%",
            types = setOf("expense"),
            typeCount = 1,
            categoryIds = setOf("food"),
            categoryCount = 1,
            tagIds = setOf("tag-essential", "tag-weekend"),
            tagCount = 2,
            startMillis = 1_700_000_000_000,
            endMillis = 1_700_000_200_000,
            minAmountMinor = 1_000,
            maxAmountMinor = 2_000,
        ).first().map { it.id }

        assertEquals(listOf("match"), observedIds)
    }

    @Test
    fun deletingCategory_nullsTransactionCategory() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction())

        categoryDao.deleteById("category-food")

        assertNull(transactionDao.getById("transaction-1")?.categoryId)
    }

    private fun TransactionDao.searchByDateDescending(
        keywordPattern: String? = null,
        types: Set<String> = setOf(NO_FILTER_PLACEHOLDER),
        typeCount: Int = 0,
        categoryIds: Set<String> = setOf(NO_FILTER_PLACEHOLDER),
        categoryCount: Int = 0,
        tagIds: Set<String> = setOf(NO_FILTER_PLACEHOLDER),
        tagCount: Int = 0,
        startMillis: Long? = null,
        endMillis: Long? = null,
        minAmountMinor: Long? = null,
        maxAmountMinor: Long? = null,
    ) = searchTransactionsByDateDescending(
        keywordPattern = keywordPattern,
        types = types,
        typeCount = typeCount,
        categoryIds = categoryIds,
        categoryCount = categoryCount,
        tagIds = tagIds,
        tagCount = tagCount,
        startMillis = startMillis,
        endMillis = endMillis,
        minAmountMinor = minAmountMinor,
        maxAmountMinor = maxAmountMinor,
    )

    private fun TransactionDao.searchByDateAscending(
        keywordPattern: String? = null,
    ) = searchTransactionsByDateAscending(
        keywordPattern = keywordPattern,
        types = setOf(NO_FILTER_PLACEHOLDER),
        typeCount = 0,
        categoryIds = setOf(NO_FILTER_PLACEHOLDER),
        categoryCount = 0,
        tagIds = setOf(NO_FILTER_PLACEHOLDER),
        tagCount = 0,
        startMillis = null,
        endMillis = null,
        minAmountMinor = null,
        maxAmountMinor = null,
    )

    private companion object {
        const val NO_FILTER_PLACEHOLDER = "__folentra_no_filter__"
    }
}
