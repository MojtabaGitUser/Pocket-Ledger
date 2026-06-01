package com.mojtaba.pocketledger.core.data.repository.local

import com.mojtaba.pocketledger.core.data.mapper.asEntity
import com.mojtaba.pocketledger.core.data.mapper.asExternalModel
import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.repository.BudgetRepository
import com.mojtaba.pocketledger.core.data.repository.contract.SyncState
import com.mojtaba.pocketledger.core.database.dao.BudgetDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LocalBudgetRepository(
    private val budgetDao: BudgetDao,
) : BudgetRepository {
    override val repositoryName: String = "budgets"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(budget: LedgerBudget) {
        budgetDao.insert(budget.asEntity())
    }

    override suspend fun insertAll(budgets: List<LedgerBudget>) {
        budgetDao.insertAll(budgets.map { it.asEntity() })
    }

    override suspend fun upsert(budget: LedgerBudget) {
        budgetDao.upsert(budget.asEntity())
    }

    override suspend fun upsertAll(budgets: List<LedgerBudget>) {
        budgetDao.upsertAll(budgets.map { it.asEntity() })
    }

    override suspend fun update(budget: LedgerBudget) {
        budgetDao.update(budget.asEntity())
    }

    override suspend fun delete(budget: LedgerBudget) {
        budgetDao.delete(budget.asEntity())
    }

    override suspend fun deleteById(id: String): Boolean = budgetDao.deleteById(id) > 0

    override suspend fun getById(id: String): LedgerBudget? =
        budgetDao.getById(id)?.asExternalModel()

    override fun observeById(id: String): Flow<LedgerBudget?> =
        budgetDao.observeById(id).map { it?.asExternalModel() }

    override fun observeBudgets(): Flow<List<LedgerBudget>> =
        budgetDao.observeBudgets().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeActiveBudgets(): Flow<List<LedgerBudget>> =
        budgetDao.observeActiveBudgets().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeBudgetsByCategory(categoryId: String): Flow<List<LedgerBudget>> =
        budgetDao.observeBudgetsByCategory(categoryId).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeBudgetsByPeriodRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<LedgerBudget>> =
        budgetDao.observeBudgetsByPeriodRange(startInclusive, endInclusive).map { entities ->
            entities.map { it.asExternalModel() }
        }
}
