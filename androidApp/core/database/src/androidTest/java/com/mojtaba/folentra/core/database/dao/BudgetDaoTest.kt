package com.mojtaba.folentra.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.database.FolentraDatabase
import com.mojtaba.folentra.core.database.testBudget
import com.mojtaba.folentra.core.database.testCategory
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
class BudgetDaoTest {
    private lateinit var database: FolentraDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var budgetDao: BudgetDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FolentraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = database.categoryDao()
        budgetDao = database.budgetDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeActiveBudgets_excludesInactiveBudgets() = runTest {
        categoryDao.insert(testCategory())
        budgetDao.insert(testBudget(id = "active"))
        budgetDao.insert(testBudget(id = "inactive", isActive = false))

        val budgets = budgetDao.observeActiveBudgets().first()

        assertEquals(listOf("active"), budgets.map { it.id })
    }

    @Test
    fun insertGetUpdateDelete_roundTripsBudget() = runTest {
        categoryDao.insert(testCategory())
        budgetDao.insert(testBudget(id = "budget", isActive = true))

        assertEquals(50_000L, budgetDao.getById("budget")?.amountMinor)

        budgetDao.update(testBudget(id = "budget", isActive = false).copy(amountMinor = 75_000))
        assertEquals(75_000L, budgetDao.getById("budget")?.amountMinor)

        budgetDao.deleteById("budget")
        assertNull(budgetDao.getById("budget"))
    }

    @Test
    fun observeById_emitsCreateUpdateDeleteChanges() = runTest {
        categoryDao.insert(testCategory())

        val emissions = mutableListOf<com.mojtaba.folentra.core.database.model.BudgetEntity?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            budgetDao.observeById("budget")
                .take(4)
                .toList(emissions)
        }
        advanceUntilIdle()

        budgetDao.insert(testBudget(id = "budget"))
        advanceUntilIdle()
        budgetDao.update(testBudget(id = "budget").copy(amountMinor = 75_000))
        advanceUntilIdle()
        budgetDao.deleteById("budget")
        advanceUntilIdle()

        assertEquals(listOf(null, 50_000L, 75_000L, null), emissions.map { it?.amountMinor })
    }

    @Test
    fun observeBudgetsByPeriodRange_returnsOverlappingBudgets() = runTest {
        categoryDao.insert(testCategory())
        budgetDao.insert(testBudget(id = "matching"))
        budgetDao.insert(
            testBudget(
                id = "outside",
                categoryId = "category-food",
            ).copy(
                periodStart = 1_800_000_000_000,
                periodEnd = 1_802_000_000_000,
            ),
        )

        val budgets = budgetDao.observeBudgetsByPeriodRange(
            startInclusive = 1_700_100_000_000,
            endInclusive = 1_700_200_000_000,
        ).first()

        assertEquals(listOf("matching"), budgets.map { it.id })
    }

    @Test
    fun observeBudgetsByCategory_filtersCategoryBudgets() = runTest {
        categoryDao.insert(testCategory(id = "food", name = "Food"))
        categoryDao.insert(testCategory(id = "travel", name = "Travel"))
        budgetDao.insert(testBudget(id = "food-budget", categoryId = "food"))
        budgetDao.insert(testBudget(id = "travel-budget", categoryId = "travel"))

        val budgets = budgetDao.observeBudgetsByCategory("food").first()

        assertEquals(listOf("food-budget"), budgets.map { it.id })
    }

    @Test
    fun deletingCategory_nullsBudgetCategory() = runTest {
        categoryDao.insert(testCategory())
        budgetDao.insert(testBudget())

        categoryDao.deleteById("category-food")

        assertNull(budgetDao.getById("budget-food")?.categoryId)
    }
}
