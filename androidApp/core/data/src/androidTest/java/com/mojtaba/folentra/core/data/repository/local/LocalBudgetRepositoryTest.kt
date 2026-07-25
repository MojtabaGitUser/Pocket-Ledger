package com.mojtaba.folentra.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.database.FolentraDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalBudgetRepositoryTest {
    private lateinit var database: FolentraDatabase
    private lateinit var categoryRepository: LocalCategoryRepository
    private lateinit var budgetRepository: LocalBudgetRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FolentraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryRepository = LocalCategoryRepository(database.categoryDao())
        budgetRepository = LocalBudgetRepository(database.budgetDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeActiveBudgets_excludesInactiveBudgets() = runTest {
        categoryRepository.insert(testCategory())
        budgetRepository.insert(testBudget(id = "active"))
        budgetRepository.insert(testBudget(id = "inactive", isActive = false))

        val budgets = budgetRepository.observeActiveBudgets().first()

        assertEquals(listOf("active"), budgets.map { it.id })
    }

    @Test
    fun insertGetUpdateDelete_roundTripsBudgetThroughRoom() = runTest {
        categoryRepository.insert(testCategory())
        budgetRepository.insert(testBudget(id = "budget"))

        assertEquals(50_000L, budgetRepository.getById("budget")?.amountMinor)

        budgetRepository.update(testBudget(id = "budget").copy(amountMinor = 75_000))
        assertEquals(75_000L, budgetRepository.getById("budget")?.amountMinor)

        val deleted = budgetRepository.deleteById("budget")

        assertEquals(true, deleted)
        assertNull(budgetRepository.getById("budget"))
    }

    @Test
    fun observeById_reflectsCreateUpdateDeleteChanges() = runTest {
        categoryRepository.insert(testCategory())

        val observedBudget = budgetRepository.observeById("budget")
        assertNull(observedBudget.first())

        budgetRepository.insert(testBudget(id = "budget"))
        assertEquals(50_000L, observedBudget.first()?.amountMinor)

        budgetRepository.update(testBudget(id = "budget").copy(amountMinor = 75_000))
        assertEquals(75_000L, observedBudget.first()?.amountMinor)

        budgetRepository.deleteById("budget")
        assertNull(observedBudget.first())
    }

    @Test
    fun observeBudgetsByPeriodRange_returnsOverlappingBudgets() = runTest {
        categoryRepository.insert(testCategory())
        budgetRepository.insert(testBudget(id = "matching"))
        budgetRepository.insert(
            testBudget(id = "outside").copy(
                periodStart = 1_800_000_000_000,
                periodEnd = 1_802_000_000_000,
            ),
        )

        val budgets = budgetRepository.observeBudgetsByPeriodRange(
            startInclusive = 1_700_100_000_000,
            endInclusive = 1_700_200_000_000,
        ).first()

        assertEquals(listOf("matching"), budgets.map { it.id })
    }

    @Test
    fun observeBudgetsByCategory_returnsMatchingLocalCategoryBudget() = runTest {
        categoryRepository.insert(testCategory(id = "food", name = "Food"))
        categoryRepository.insert(testCategory(id = "travel", name = "Travel"))
        budgetRepository.insert(testBudget(id = "food-budget", categoryId = "food"))
        budgetRepository.insert(testBudget(id = "travel-budget", categoryId = "travel"))

        val budgets = budgetRepository.observeBudgetsByCategory("food").first()

        assertEquals(listOf("food-budget"), budgets.map { it.id })
    }

    @Test
    fun deletingCategory_nullsBudgetCategoryThroughRoom() = runTest {
        categoryRepository.insert(testCategory())
        budgetRepository.insert(testBudget())

        categoryRepository.deleteById("category-food")

        assertNull(budgetRepository.getById("budget-food")?.categoryId)
    }
}
