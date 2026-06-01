package com.mojtaba.pocketledger.core.data.repository

import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.repository.contract.OfflineFirstRepository
import kotlinx.coroutines.flow.Flow

interface CategoryRepository : OfflineFirstRepository {
    suspend fun insert(category: LedgerCategory)

    suspend fun insertAll(categories: List<LedgerCategory>)

    suspend fun upsert(category: LedgerCategory)

    suspend fun upsertAll(categories: List<LedgerCategory>)

    suspend fun update(category: LedgerCategory)

    suspend fun delete(category: LedgerCategory)

    suspend fun deleteById(id: String): Boolean

    suspend fun getById(id: String): LedgerCategory?

    fun observeById(id: String): Flow<LedgerCategory?>

    fun observeAll(): Flow<List<LedgerCategory>>

    fun observeActiveCategories(): Flow<List<LedgerCategory>>

    fun observeActiveCategoriesByType(type: String): Flow<List<LedgerCategory>>
}
