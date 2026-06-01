package com.mojtaba.pocketledger.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class LocalCategoryRepositoryTest {
    private lateinit var database: PocketLedgerDatabase
    private lateinit var repository: LocalCategoryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PocketLedgerDatabase::class.java)
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
    fun upsert_updatesExistingCategory() = runTest {
        repository.upsert(testCategory(id = "category", name = "Old"))
        repository.upsert(testCategory(id = "category", name = "New", isActive = false))

        val category = repository.getById("category")

        assertEquals("New", category?.name)
        assertFalse(category?.isActive ?: true)
    }
}
