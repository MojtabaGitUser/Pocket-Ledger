package com.mojtaba.pocketledger.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import com.mojtaba.pocketledger.core.database.testCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

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
    fun upsert_updatesExistingCategory() = runTest {
        categoryDao.upsert(testCategory(id = "category", name = "Old"))
        categoryDao.upsert(testCategory(id = "category", name = "New", isActive = false))

        val category = categoryDao.getById("category")

        assertEquals("New", category?.name)
        assertFalse(category?.isActive ?: true)
    }
}
