package com.mojtaba.pocketledger.core.data.repository

import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun insert(transaction: LedgerTransaction)

    suspend fun insertAll(transactions: List<LedgerTransaction>)

    suspend fun upsert(transaction: LedgerTransaction)

    suspend fun upsertAll(transactions: List<LedgerTransaction>)

    suspend fun update(transaction: LedgerTransaction)

    suspend fun delete(transaction: LedgerTransaction)

    suspend fun deleteById(id: String): Boolean

    suspend fun getById(id: String): LedgerTransaction?

    fun observeById(id: String): Flow<LedgerTransaction?>

    fun observeRecentTransactions(limit: Int): Flow<List<LedgerTransaction>>

    fun observeTransactionsByDateRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<LedgerTransaction>>

    fun observeTransactionsByCategory(categoryId: String): Flow<List<LedgerTransaction>>

    fun observeTransactionsByType(type: String): Flow<List<LedgerTransaction>>

    fun observeTransactionsByTag(tagId: String): Flow<List<LedgerTransaction>>
}
