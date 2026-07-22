package com.mojtaba.folentra.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.database.FolentraDatabase
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
class LocalCategoryRepositoryTest {
    private lateinit var database: FolentraDatabase
    private lateinit var repository: LocalCategoryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FolentraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LocalCategoryRepository(database.categoryDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeActiveCategories_excludesInactiveCategories() = runTest {
        repository.insert(testCategory(id = "active", name = "Active"))
        repository.insert(testCategory(id = "inactive", name = "Inactive", isActive = false))

        val categories = repository.observeActiveCategories().first()

        assertEquals(listOf("active"), categories.map { it.id })
    }

    @Test
    fun insertGetUpdateDelete_roundTripsCategoryThroughRoom() = runTest {
        repository.insert(testCategory(id = "category", name = "Old"))

        assertEquals("Old", repository.getById("category")?.name)

        repository.update(testCategory(id = "category", name = "New"))
        assertEquals("New", repository.getById("category")?.name)

        val deleted = repository.deleteById("category")

        assertEquals(true, deleted)
        assertNull(repository.getById("category"))
    }

    @Test
    fun upsert_updatesExistingCategory() = runTest {
        repository.upsert(testCategory(id = "category", name = "Old"))
        repository.upsert(testCategory(id = "category", name = "New", isActive = false))

        val category = repository.getById("category")

        assertEquals("New", category?.name)
        assertFalse(category?.isActive ?: true)
    }

    @Test
    fun observeById_emitsCreateUpdateDeleteChanges() = runTest {
        val emissions = mutableListOf<com.mojtaba.folentra.core.data.model.LedgerCategory?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeById("category")
                .take(4)
                .toList(emissions)
        }
        advanceUntilIdle()

        repository.insert(testCategory(id = "category", name = "Old"))
        advanceUntilIdle()
        repository.update(testCategory(id = "category", name = "New"))
        advanceUntilIdle()
        repository.deleteById("category")
        advanceUntilIdle()

        assertEquals(listOf(null, "Old", "New", null), emissions.map { it?.name })
    }

    @Test
    fun observeActiveCategoriesByType_returnsLocalActiveExpenseCategories() = runTest {
        repository.insert(testCategory(id = "expense-active", type = "expense", isActive = true))
        repository.insert(testCategory(id = "expense-inactive", name = "Inactive", type = "expense", isActive = false))
        repository.insert(testCategory(id = "income-active", name = "Salary", type = "income", isActive = true))

        val categories = repository.observeActiveCategoriesByType("expense").first()

        assertEquals(listOf("expense-active"), categories.map { it.id })
    }
}
