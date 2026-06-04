package com.mojtaba.pocketledger.core.data.seed

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.repository.local.LocalBudgetRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalCategoryRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalTagRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalTransactionRepository
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DemoDataSeederTest {
    private lateinit var database: PocketLedgerDatabase
    private lateinit var categoryRepository: LocalCategoryRepository
    private lateinit var transactionRepository: LocalTransactionRepository
    private lateinit var budgetRepository: LocalBudgetRepository
    private lateinit var tagRepository: LocalTagRepository
    private lateinit var seeder: DemoDataSeeder

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PocketLedgerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryRepository = LocalCategoryRepository(database.categoryDao())
        transactionRepository = LocalTransactionRepository(database.transactionDao())
        budgetRepository = LocalBudgetRepository(database.budgetDao())
        tagRepository = LocalTagRepository(database.tagDao())
        seeder = DemoDataSeeder(
            categoryRepository = categoryRepository,
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            tagRepository = tagRepository,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun demoSeedData_hasStableDemoIdsAndRealisticCounts() {
        val ids = buildList {
            addAll(DemoSeedData.categories.map { it.id })
            addAll(DemoSeedData.budgets.map { it.id })
            addAll(DemoSeedData.tags.map { it.id })
            addAll(DemoSeedData.transactions.map { it.id })
        }

        assertTrue(ids.all { it.startsWith(DemoSeedIds.DemoIdPrefix) })
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(8, DemoSeedData.categories.size)
        assertEquals(4, DemoSeedData.budgets.size)
        assertEquals(5, DemoSeedData.tags.size)
        assertTrue(DemoSeedData.transactions.size in 12..20)
        assertTrue(DemoSeedData.transactions.any { it.type == DemoSeedData.IncomeType })
        assertTrue(DemoSeedData.transactions.any { it.type == DemoSeedData.ExpenseType })
        assertTrue(DemoSeedData.transactionTagLinks.isNotEmpty())
        assertTrue(DemoSeedData.transactions.all { it.currencyCode == DemoSeedData.CurrencyCode })
    }

    @Test
    fun seedDemoData_insertsDemoRecordsAndReportsPlannedCounts() = runTest {
        assertFalse(seeder.isDemoDataPresent())

        val result = seeder.seedDemoData()

        assertEquals(DemoSeedData.result, result)
        assertTrue(seeder.isDemoDataPresent())
        assertEquals(DemoSeedData.categories.size, categoryRepository.observeAll().first().size)
        assertEquals(DemoSeedData.budgets.size, budgetRepository.observeBudgets().first().size)
        assertEquals(DemoSeedData.tags.size, tagRepository.observeTags().first().size)
        assertEquals(DemoSeedData.transactions.size, transactionRepository.observeRecentTransactions(50).first().size)
        assertNotNull(categoryRepository.getById(DemoSeedIds.CategoryGroceries))
        assertNotNull(budgetRepository.getById(DemoSeedIds.BudgetGroceries))
        assertNotNull(tagRepository.getById(DemoSeedIds.TagEssential))
        assertNotNull(transactionRepository.getById(DemoSeedIds.TransactionRent))
    }

    @Test
    fun seedDemoData_isIdempotent() = runTest {
        seeder.seedDemoData()
        seeder.seedDemoData()

        assertEquals(DemoSeedData.categories.size, categoryRepository.observeAll().first().size)
        assertEquals(DemoSeedData.budgets.size, budgetRepository.observeBudgets().first().size)
        assertEquals(DemoSeedData.tags.size, tagRepository.observeTags().first().size)
        assertEquals(DemoSeedData.transactions.size, transactionRepository.observeRecentTransactions(50).first().size)
        DemoSeedData.transactionTagLinks
            .groupBy { it.transactionId }
            .forEach { (transactionId, links) ->
                assertEquals(
                    links.map { it.tagId }.sorted(),
                    tagRepository.observeTagsForTransaction(transactionId).first().map { it.id }.sorted(),
                )
            }
    }

    @Test
    fun seedDemoData_doesNotDeleteUnrelatedRecords() = runTest {
        categoryRepository.insert(unrelatedCategory())
        tagRepository.insert(unrelatedTag())
        transactionRepository.insert(unrelatedTransaction())
        budgetRepository.insert(unrelatedBudget())

        seeder.seedDemoData()

        assertNotNull(categoryRepository.getById("user-category-health"))
        assertNotNull(tagRepository.getById("user-tag-medical"))
        assertNotNull(transactionRepository.getById("user-transaction-prescription"))
        assertNotNull(budgetRepository.getById("user-budget-health"))
        assertEquals(DemoSeedData.categories.size + 1, categoryRepository.observeAll().first().size)
        assertEquals(DemoSeedData.budgets.size + 1, budgetRepository.observeBudgets().first().size)
        assertEquals(DemoSeedData.tags.size + 1, tagRepository.observeTags().first().size)
        assertEquals(DemoSeedData.transactions.size + 1, transactionRepository.observeRecentTransactions(50).first().size)
    }

    @Test
    fun seedDemoData_doesNotDuplicateTagLinks() = runTest {
        seeder.seedDemoData()
        seeder.seedDemoData()

        val diningTags = tagRepository.observeTagsForTransaction(DemoSeedIds.TransactionDiningA).first()

        assertEquals(listOf(DemoSeedIds.TagFamily), diningTags.map { it.id })
    }

    private fun unrelatedCategory(): LedgerCategory = LedgerCategory(
        id = "user-category-health",
        name = "Health",
        type = DemoSeedData.ExpenseType,
        colorHex = "#00796B",
        iconName = "local_pharmacy",
        sortOrder = 100,
        isActive = true,
        createdAt = 1_769_900_000_000L,
        updatedAt = 1_769_900_000_000L,
    )

    private fun unrelatedBudget(): LedgerBudget = LedgerBudget(
        id = "user-budget-health",
        name = "Health budget",
        amountMinor = 12_000,
        currencyCode = DemoSeedData.CurrencyCode,
        periodType = DemoSeedData.PeriodTypeMonthly,
        periodStart = DemoSeedData.PeriodStart,
        periodEnd = DemoSeedData.PeriodEnd,
        categoryId = "user-category-health",
        isActive = true,
        createdAt = 1_769_900_000_000L,
        updatedAt = 1_769_900_000_000L,
    )

    private fun unrelatedTag(): LedgerTag = LedgerTag(
        id = "user-tag-medical",
        name = "medical",
        colorHex = "#AD1457",
        createdAt = 1_769_900_000_000L,
        updatedAt = 1_769_900_000_000L,
    )

    private fun unrelatedTransaction(): LedgerTransaction = LedgerTransaction(
        id = "user-transaction-prescription",
        amountMinor = -3_250,
        currencyCode = DemoSeedData.CurrencyCode,
        type = DemoSeedData.ExpenseType,
        occurredAt = 1_770_200_000_000L,
        categoryId = "user-category-health",
        merchant = "Community Pharmacy",
        note = "Prescription refill",
        source = "manual",
        isRecurring = false,
        createdAt = 1_769_900_000_000L,
        updatedAt = 1_769_900_000_000L,
    )
}
