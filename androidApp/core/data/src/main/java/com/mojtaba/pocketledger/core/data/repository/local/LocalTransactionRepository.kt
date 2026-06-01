package com.mojtaba.pocketledger.core.data.repository.local

import com.mojtaba.pocketledger.core.data.mapper.asEntity
import com.mojtaba.pocketledger.core.data.mapper.asExternalModel
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.data.repository.contract.SyncState
import com.mojtaba.pocketledger.core.database.dao.TransactionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LocalTransactionRepository(
    private val transactionDao: TransactionDao,
) : TransactionRepository {
    override val repositoryName: String = "transactions"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(transaction: LedgerTransaction) {
        transactionDao.insert(transaction.asEntity())
    }

    override suspend fun insertAll(transactions: List<LedgerTransaction>) {
        transactionDao.insertAll(transactions.map { it.asEntity() })
    }

    override suspend fun upsert(transaction: LedgerTransaction) {
        transactionDao.upsert(transaction.asEntity())
    }

    override suspend fun upsertAll(transactions: List<LedgerTransaction>) {
        transactionDao.upsertAll(transactions.map { it.asEntity() })
    }

    override suspend fun update(transaction: LedgerTransaction) {
        transactionDao.update(transaction.asEntity())
    }

    override suspend fun delete(transaction: LedgerTransaction) {
        transactionDao.delete(transaction.asEntity())
    }

    override suspend fun deleteById(id: String): Boolean = transactionDao.deleteById(id) > 0

    override suspend fun getById(id: String): LedgerTransaction? =
        transactionDao.getById(id)?.asExternalModel()

    override fun observeById(id: String): Flow<LedgerTransaction?> =
        transactionDao.observeById(id).map { it?.asExternalModel() }

    override fun observeRecentTransactions(limit: Int): Flow<List<LedgerTransaction>> =
        transactionDao.observeRecentTransactions(limit).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeTransactionsByDateRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<LedgerTransaction>> =
        transactionDao.observeTransactionsByDateRange(startInclusive, endInclusive).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeTransactionsByCategory(categoryId: String): Flow<List<LedgerTransaction>> =
        transactionDao.observeTransactionsByCategory(categoryId).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeTransactionsByType(type: String): Flow<List<LedgerTransaction>> =
        transactionDao.observeTransactionsByType(type).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun observeTransactionsByTag(tagId: String): Flow<List<LedgerTransaction>> =
        transactionDao.observeTransactionsByTag(tagId).map { entities ->
            entities.map { it.asExternalModel() }
        }
}
