package com.mojtaba.folentra.core.data.seed

import com.mojtaba.folentra.core.data.model.LedgerCategory
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.contract.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultCategorySeederTest {
    private val repository = RecordingCategoryRepository()
    private val seeder = DefaultCategorySeeder(
        categoryRepository = repository,
        currentTimeMillis = { CREATED_AT },
    )

    @Test
    fun seedIfEmpty_insertsUsableDefaultCatalogue() = runBlocking {
        val insertedCount = seeder.seedIfEmpty()
        val categories = repository.snapshot()

        assertEquals(DefaultCategoryCatalog.categories(CREATED_AT).size, insertedCount)
        assertEquals(DefaultCategoryCatalog.categories(CREATED_AT), categories)
        assertTrue(categories.any { it.type == DefaultCategoryCatalog.IncomeType && it.isActive })
        assertTrue(categories.any { it.type == DefaultCategoryCatalog.ExpenseType && it.isActive })
    }

    @Test
    fun seedIfEmpty_isIdempotent() = runBlocking {
        seeder.seedIfEmpty()

        val insertedCount = seeder.seedIfEmpty()

        assertEquals(0, insertedCount)
        assertEquals(
            DefaultCategoryCatalog.categories(CREATED_AT),
            repository.snapshot(),
        )
    }

    @Test
    fun seedIfEmpty_preservesExistingCatalogue() = runBlocking {
        val existingCategory = LedgerCategory(
            id = "user-category-custom",
            name = "My custom category",
            type = DefaultCategoryCatalog.ExpenseType,
            colorHex = "#000000",
            iconName = "star",
            sortOrder = 1,
            isActive = false,
            createdAt = 1L,
            updatedAt = 2L,
        )
        repository.insert(existingCategory)

        val insertedCount = seeder.seedIfEmpty()

        assertEquals(0, insertedCount)
        assertEquals(listOf(existingCategory), repository.snapshot())
    }

    private class RecordingCategoryRepository : CategoryRepository {
        private val categories = MutableStateFlow<Map<String, LedgerCategory>>(emptyMap())

        override val repositoryName: String = "recording-categories"

        override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

        override suspend fun insert(category: LedgerCategory) {
            categories.value += category.id to category
        }

        override suspend fun insertAll(categories: List<LedgerCategory>) {
            this.categories.value += categories.associateBy { it.id }
        }

        override suspend fun insertAllIfAbsent(categories: List<LedgerCategory>): Int {
            val missingCategories = categories.filterNot { it.id in this.categories.value }
            this.categories.value += missingCategories.associateBy { it.id }
            return missingCategories.size
        }

        override suspend fun upsert(category: LedgerCategory) = insert(category)

        override suspend fun upsertAll(categories: List<LedgerCategory>) = insertAll(categories)

        override suspend fun update(category: LedgerCategory) = insert(category)

        override suspend fun delete(category: LedgerCategory) {
            categories.value -= category.id
        }

        override suspend fun deleteById(id: String): Boolean {
            val existed = id in categories.value
            categories.value -= id
            return existed
        }

        override suspend fun getById(id: String): LedgerCategory? = categories.value[id]

        override fun observeById(id: String): Flow<LedgerCategory?> = categories.map { it[id] }

        override fun observeAll(): Flow<List<LedgerCategory>> = categories.map { categoryMap ->
            categoryMap.values.sortedForDisplay()
        }

        override fun observeActiveCategories(): Flow<List<LedgerCategory>> = categories.map { categoryMap ->
            categoryMap.values.filter { it.isActive }.sortedForDisplay()
        }

        override fun observeActiveCategoriesByType(type: String): Flow<List<LedgerCategory>> =
            observeActiveCategories().map { categoryList -> categoryList.filter { it.type == type } }

        fun snapshot(): List<LedgerCategory> = categories.value.values.sortedForDisplay()

        private fun Iterable<LedgerCategory>.sortedForDisplay(): List<LedgerCategory> =
            sortedWith(compareBy<LedgerCategory> { it.sortOrder }.thenBy { it.name }.thenBy { it.id })
    }

    private companion object {
        const val CREATED_AT = 1_789_000_000_000L
    }
}
