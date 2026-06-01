package com.mojtaba.pocketledger.core.data.repository.local

import com.mojtaba.pocketledger.core.data.mapper.asEntity
import com.mojtaba.pocketledger.core.data.mapper.asExternalModel
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.database.dao.CategoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalCategoryRepository(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
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
