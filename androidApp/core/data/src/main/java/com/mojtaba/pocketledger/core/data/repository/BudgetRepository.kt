package com.mojtaba.pocketledger.core.data.repository

import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.repository.contract.OfflineFirstRepository
import kotlinx.coroutines.flow.Flow

interface BudgetRepository : OfflineFirstRepository {
    suspend fun insert(budget: LedgerBudget)

    suspend fun insertAll(budgets: List<LedgerBudget>)

    suspend fun upsert(budget: LedgerBudget)

    suspend fun upsertAll(budgets: List<LedgerBudget>)

    suspend fun update(budget: LedgerBudget)

    suspend fun delete(budget: LedgerBudget)

    suspend fun deleteById(id: String): Boolean

    suspend fun getById(id: String): LedgerBudget?

    fun observeById(id: String): Flow<LedgerBudget?>

    fun observeBudgets(): Flow<List<LedgerBudget>>

    fun observeActiveBudgets(): Flow<List<LedgerBudget>>

    fun observeBudgetsByCategory(categoryId: String): Flow<List<LedgerBudget>>

    fun observeBudgetsByPeriodRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<LedgerBudget>>
}
