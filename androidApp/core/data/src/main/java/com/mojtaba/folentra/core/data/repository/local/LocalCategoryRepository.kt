package com.mojtaba.folentra.core.data.repository.local

import com.mojtaba.folentra.core.data.mapper.asEntity
import com.mojtaba.folentra.core.data.mapper.asExternalModel
import com.mojtaba.folentra.core.data.model.LedgerCategory
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.contract.SyncState
import com.mojtaba.folentra.core.database.dao.CategoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LocalCategoryRepository(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override val repositoryName: String = "categories"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(category: LedgerCategory) {
        categoryDao.insert(category.asEntity())
    }

    override suspend fun insertAll(categories: List<LedgerCategory>) {
        categoryDao.insertAll(categories.map { it.asEntity() })
    }

    override suspend fun upsert(category: LedgerCategory) {
        categoryDao.upsert(category.asEntity())
    }

    override suspend fun upsertAll(categories: List<LedgerCategory>) {
        categoryDao.upsertAll(categories.map { it.asEntity() })
    }

    override suspend fun update(category: LedgerCategory) {
        categoryDao.update(category.asEntity())
    }

    override suspend fun delete(category: LedgerCategory) {
        categoryDao.delete(category.asEntity())
    }

    override suspend fun deleteById(id: String): Boolean = categoryDao.deleteById(id) > 0

    override suspend fun getById(id: String): LedgerCategory? =
        categoryDao.getById(id)?.asExternalModel()

    override fun observeById(id: String): Flow<LedgerCategory?> =
        categoryDao.observeById(id).map { it?.asExternalModel() }

    override fun observeAll(): Flow<List<LedgerCategory>> =
        categoryDao.observeAll().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeActiveCategories(): Flow<List<LedgerCategory>> =
        categoryDao.observeActiveCategories().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeActiveCategoriesByType(type: String): Flow<List<LedgerCategory>> =
        categoryDao.observeActiveCategoriesByType(type).map { entities ->
            entities.map { it.asExternalModel() }
        }
}
