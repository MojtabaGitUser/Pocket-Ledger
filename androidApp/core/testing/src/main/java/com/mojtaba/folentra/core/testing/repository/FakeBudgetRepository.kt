package com.mojtaba.folentra.core.testing.repository

import com.mojtaba.folentra.core.data.model.LedgerBudget
import com.mojtaba.folentra.core.data.repository.BudgetRepository
import com.mojtaba.folentra.core.data.repository.contract.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeBudgetRepository(
    initialBudgets: List<LedgerBudget> = emptyList(),
) : BudgetRepository {
    private val budgets = MutableStateFlow(initialBudgets.associateBy { it.id })

    override val repositoryName: String = "fake-budgets"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(budget: LedgerBudget) = upsert(budget)

    override suspend fun insertAll(budgets: List<LedgerBudget>) = upsertAll(budgets)

    override suspend fun upsert(budget: LedgerBudget) {
        budgets.update { it + (budget.id to budget) }
    }

    override suspend fun upsertAll(budgets: List<LedgerBudget>) {
        this.budgets.update { current -> current + budgets.associateBy { it.id } }
    }

    override suspend fun update(budget: LedgerBudget) = upsert(budget)

    override suspend fun delete(budget: LedgerBudget) {
        budgets.update { it - budget.id }
    }

    override suspend fun deleteById(id: String): Boolean {
        val existed = id in budgets.value
        budgets.update { it - id }
        return existed
    }

    override suspend fun getById(id: String): LedgerBudget? = budgets.value[id]

    override fun observeById(id: String): Flow<LedgerBudget?> = budgets.map { it[id] }

    override fun observeBudgets(): Flow<List<LedgerBudget>> = budgets.map { it.values.sortedForDisplay() }

    override fun observeActiveBudgets(): Flow<List<LedgerBudget>> =
        budgets.map { budgetMap -> budgetMap.values.filter { it.isActive }.sortedForDisplay() }

    override fun observeBudgetsByCategory(categoryId: String): Flow<List<LedgerBudget>> =
        observeBudgets().map { budgets -> budgets.filter { it.categoryId == categoryId } }

    override fun observeBudgetsByPeriodRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<LedgerBudget>> =
        observeBudgets().map { budgets ->
            budgets.filter { it.periodStart <= endInclusive && it.periodEnd >= startInclusive }
        }

    fun snapshot(): List<LedgerBudget> = budgets.value.values.sortedForDisplay()

    private fun Iterable<LedgerBudget>.sortedForDisplay(): List<LedgerBudget> =
        sortedWith(
            compareByDescending<LedgerBudget> { it.isActive }
                .thenByDescending { it.periodStart }
                .thenBy { it.name.lowercase() }
                .thenBy { it.id },
        )
}
