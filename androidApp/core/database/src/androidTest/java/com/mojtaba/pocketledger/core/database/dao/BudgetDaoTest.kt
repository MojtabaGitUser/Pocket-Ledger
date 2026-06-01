package com.mojtaba.pocketledger.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import com.mojtaba.pocketledger.core.database.testBudget
import com.mojtaba.pocketledger.core.database.testCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BudgetDaoTest {
    private lateinit var database: PocketLedgerDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var budgetDao: BudgetDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PocketLedgerDatabase::class.java)
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
}
