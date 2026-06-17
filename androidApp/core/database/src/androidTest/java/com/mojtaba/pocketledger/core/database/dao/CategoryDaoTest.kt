package com.mojtaba.pocketledger.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import com.mojtaba.pocketledger.core.database.testCategory
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDaoTest {
    private lateinit var database: PocketLedgerDatabase
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PocketLedgerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeActiveCategories_excludesInactiveCategories() = runTest {
        categoryDao.insert(testCategory(id = "active", name = "Active"))
        categoryDao.insert(testCategory(id = "inactive", name = "Inactive", isActive = false))

        val categories = categoryDao.observeActiveCategories().first()

        assertEquals(listOf("active"), categories.map { it.id })
    }

    @Test
    fun insertGetUpdateDelete_roundTripsCategory() = runTest {
        categoryDao.insert(testCategory(id = "category", name = "Old"))

        assertEquals("Old", categoryDao.getById("category")?.name)

        categoryDao.update(testCategory(id = "category", name = "New"))
        assertEquals("New", categoryDao.getById("category")?.name)

        categoryDao.deleteById("category")
        assertNull(categoryDao.getById("category"))
    }

    @Test
    fun upsert_updatesExistingCategory() = runTest {
        categoryDao.upsert(testCategory(id = "category", name = "Old"))
        categoryDao.upsert(testCategory(id = "category", name = "New", isActive = false))

        val category = categoryDao.getById("category")

        assertEquals("New", category?.name)
        assertFalse(category?.isActive ?: true)
    }

    @Test
    fun observeById_emitsCreateUpdateDeleteChanges() = runTest {
        val emissions = mutableListOf<com.mojtaba.pocketledger.core.database.model.CategoryEntity?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            categoryDao.observeById("category")
                .take(4)
                .toList(emissions)
        }
        advanceUntilIdle()

        categoryDao.insert(testCategory(id = "category", name = "Old"))
        advanceUntilIdle()
        categoryDao.update(testCategory(id = "category", name = "New"))
        advanceUntilIdle()
        categoryDao.deleteById("category")
        advanceUntilIdle()

        assertEquals(listOf(null, "Old", "New", null), emissions.map { it?.name })
    }

    @Test
    fun observeActiveCategoriesByType_filtersInactiveAndOtherTypes() = runTest {
        categoryDao.insert(testCategory(id = "expense-active", type = "expense", isActive = true))
        categoryDao.insert(testCategory(id = "expense-inactive", name = "Inactive", type = "expense", isActive = false))
        categoryDao.insert(testCategory(id = "income-active", name = "Salary", type = "income", isActive = true))

        val categories = categoryDao.observeActiveCategoriesByType("expense").first()

        assertEquals(listOf("expense-active"), categories.map { it.id })
    }
}
