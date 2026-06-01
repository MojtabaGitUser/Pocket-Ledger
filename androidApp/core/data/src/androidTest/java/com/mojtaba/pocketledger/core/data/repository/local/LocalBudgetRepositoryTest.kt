package com.mojtaba.pocketledger.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LocalBudgetRepositoryTest {
    private lateinit var database: PocketLedgerDatabase
    private lateinit var categoryRepository: LocalCategoryRepository
    private lateinit var budgetRepository: LocalBudgetRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PocketLedgerDatabase::class.java)
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
}
