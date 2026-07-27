package com.mojtaba.folentra.core.testing.repository

import com.mojtaba.folentra.core.data.model.LedgerCategory
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.contract.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCategoryRepository(
    initialCategories: List<LedgerCategory> = emptyList(),
) : CategoryRepository {
    private val categories = MutableStateFlow(initialCategories.associateBy { it.id })

    override val repositoryName: String = "fake-categories"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(category: LedgerCategory) = upsert(category)

    override suspend fun insertAll(categories: List<LedgerCategory>) = upsertAll(categories)

    override suspend fun upsert(category: LedgerCategory) {
        categories.update { it + (category.id to category) }
    }

    override suspend fun upsertAll(categories: List<LedgerCategory>) {
        this.categories.update { current -> current + categories.associateBy { it.id } }
    }

    override suspend fun update(category: LedgerCategory) = upsert(category)

    override suspend fun delete(category: LedgerCategory) {
        categories.update { it - category.id }
    }

    override suspend fun deleteById(id: String): Boolean {
        val existed = id in categories.value
        categories.update { it - id }
        return existed
    }

    override suspend fun getById(id: String): LedgerCategory? = categories.value[id]

    override fun observeById(id: String): Flow<LedgerCategory?> = categories.map { it[id] }

    override fun observeAll(): Flow<List<LedgerCategory>> = categories.map { it.values.sortedForDisplay() }

    override fun observeActiveCategories(): Flow<List<LedgerCategory>> =
        categories.map { categoryMap ->
            categoryMap.values.filter { it.isActive }.sortedForDisplay()
        }

    override fun observeActiveCategoriesByType(type: String): Flow<List<LedgerCategory>> =
        observeActiveCategories().map { categories -> categories.filter { it.type == type } }

    fun snapshot(): List<LedgerCategory> = categories.value.values.sortedForDisplay()

    private fun Iterable<LedgerCategory>.sortedForDisplay(): List<LedgerCategory> =
        sortedWith(compareBy<LedgerCategory> { it.sortOrder }.thenBy { it.name.lowercase() }.thenBy { it.id })
}
