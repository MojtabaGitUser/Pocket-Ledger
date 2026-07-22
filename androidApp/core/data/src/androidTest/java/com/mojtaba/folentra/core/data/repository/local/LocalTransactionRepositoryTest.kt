package com.mojtaba.folentra.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.database.FolentraDatabase
import com.mojtaba.folentra.core.data.model.TransactionTagLink
import com.mojtaba.folentra.core.data.search.SearchAmountRange
import com.mojtaba.folentra.core.data.search.SearchDateRange
import com.mojtaba.folentra.core.data.search.SearchFilters
import com.mojtaba.folentra.core.data.search.SearchQuery
import com.mojtaba.folentra.core.data.search.SearchSort
import com.mojtaba.folentra.core.data.search.SearchTransactionType
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
class LocalTransactionRepositoryTest {
    private lateinit var database: FolentraDatabase
    private lateinit var categoryRepository: LocalCategoryRepository
    private lateinit var tagRepository: LocalTagRepository
    private lateinit var transactionRepository: LocalTransactionRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FolentraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryRepository = LocalCategoryRepository(database.categoryDao())
        tagRepository = LocalTagRepository(database.tagDao())
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
    fun repositoryReadsComeFromLocalRoomDatabase() = runTest {
        categoryRepository.insert(testCategory())
        database.transactionDao().insert(
            com.mojtaba.folentra.core.database.model.TransactionEntity(
                id = "dao-written",
                amountMinor = -3_000,
                currencyCode = "USD",
                type = "expense",
                occurredAt = TEST_OCCURRED_AT,
                categoryId = "category-food",
                merchant = "Local Store",
                note = "Inserted through DAO",
                source = "manual",
                isRecurring = false,
                createdAt = TEST_CREATED_AT,
                updatedAt = TEST_CREATED_AT,
            ),
        )

        val transaction = transactionRepository.getById("dao-written")

        assertEquals(-3_000L, transaction?.amountMinor)
        assertEquals("Local Store", transaction?.merchant)
    }

    @Test
    fun observeById_emitsCreateUpdateDeleteChanges() = runTest {
        categoryRepository.insert(testCategory())

        val emissions = mutableListOf<com.mojtaba.folentra.core.data.model.LedgerTransaction?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            transactionRepository.observeById("transaction-1")
                .take(4)
                .toList(emissions)
        }
        advanceUntilIdle()

        transactionRepository.insert(testTransaction())
        advanceUntilIdle()
        transactionRepository.update(testTransaction(amountMinor = -2_000))
        advanceUntilIdle()
        transactionRepository.deleteById("transaction-1")
        advanceUntilIdle()

        assertEquals(listOf(null, -1_250L, -2_000L, null), emissions.map { it?.amountMinor })
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
    fun observeTransactionsByDateRange_returnsDashboardPeriodTransactions() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(testTransaction(id = "before", occurredAt = 1_699_999_999_999))
        transactionRepository.insert(testTransaction(id = "inside", occurredAt = 1_700_000_100_000))
        transactionRepository.insert(testTransaction(id = "after", occurredAt = 1_700_000_200_001))

        val observedIds = transactionRepository.observeTransactionsByDateRange(
            startInclusive = 1_700_000_000_000,
            endInclusive = 1_700_000_200_000,
        ).first().map { it.id }

        assertEquals(listOf("inside"), observedIds)
    }

    @Test
    fun observeTransactionsByTag_reflectsRepositoryRelationshipChanges() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(testTransaction(id = "tagged"))
        tagRepository.insert(testTag(id = "tag-essential", name = "Essential"))

        assertEquals(emptyList<String>(), transactionRepository.observeTransactionsByTag("tag-essential").first().map { it.id })

        tagRepository.addTagToTransaction(TransactionTagLink("tagged", "tag-essential"))
        assertEquals(listOf("tagged"), transactionRepository.observeTransactionsByTag("tag-essential").first().map { it.id })

        tagRepository.removeTagFromTransaction("tagged", "tag-essential")
        assertEquals(emptyList<String>(), transactionRepository.observeTransactionsByTag("tag-essential").first().map { it.id })
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

    @Test
    fun searchTransactions_appliesLocalFirstFiltersForDashboardAndSearch() = runTest {
        categoryRepository.insert(testCategory(id = "food", name = "Food"))
        categoryRepository.insert(testCategory(id = "travel", name = "Travel"))
        tagRepository.insert(testTag(id = "tag-essential", name = "Essential"))
        tagRepository.insert(testTag(id = "tag-weekend", name = "Weekend"))
        transactionRepository.insert(
            testTransaction(
                id = "match",
                amountMinor = -1_500,
                occurredAt = 1_700_000_100_000,
                categoryId = "food",
                merchant = "Coffee Shop",
            ),
        )
        transactionRepository.insert(
            testTransaction(
                id = "wrong-category",
                amountMinor = -1_500,
                occurredAt = 1_700_000_100_000,
                categoryId = "travel",
                merchant = "Coffee Shop",
            ),
        )
        transactionRepository.insert(
            testTransaction(
                id = "wrong-amount",
                amountMinor = -50,
                occurredAt = 1_700_000_100_000,
                categoryId = "food",
                merchant = "Coffee Shop",
            ),
        )
        tagRepository.addTagToTransaction(TransactionTagLink("match", "tag-essential"))
        tagRepository.addTagToTransaction(TransactionTagLink("match", "tag-weekend"))
        tagRepository.addTagToTransaction(TransactionTagLink("wrong-category", "tag-essential"))

        val observedIds = transactionRepository.searchTransactions(
            SearchQuery(
                text = " coffee ",
                filters = SearchFilters(
                    transactionTypes = setOf(SearchTransactionType.Expense),
                    categoryIds = setOf("food"),
                    tagIds = setOf("tag-essential", "tag-weekend"),
                    dateRange = SearchDateRange(
                        startMillis = 1_700_000_000_000,
                        endMillis = 1_700_000_200_000,
                    ),
                    amountRange = SearchAmountRange(minMinor = 1_000, maxMinor = 2_000),
                ),
            ),
        ).first().map { it.id }

        assertEquals(listOf("match"), observedIds)
    }
}
