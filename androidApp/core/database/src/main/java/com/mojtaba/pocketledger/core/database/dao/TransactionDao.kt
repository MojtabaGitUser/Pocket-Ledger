package com.mojtaba.pocketledger.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.mojtaba.pocketledger.core.database.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: String): Flow<TransactionEntity?>

    @Query(
        """
        SELECT * FROM transactions
        ORDER BY occurred_at DESC, created_at DESC, id ASC
        LIMIT :limit
        """,
    )
    fun observeRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE occurred_at BETWEEN :startInclusive AND :endInclusive
        ORDER BY occurred_at DESC, created_at DESC, id ASC
        """,
    )
    fun observeTransactionsByDateRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE category_id = :categoryId
        ORDER BY occurred_at DESC, created_at DESC, id ASC
        """,
    )
    fun observeTransactionsByCategory(categoryId: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE type = :type
        ORDER BY occurred_at DESC, created_at DESC, id ASC
        """,
    )
    fun observeTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT transactions.* FROM transactions
        INNER JOIN transaction_tags ON transactions.id = transaction_tags.transaction_id
        WHERE transaction_tags.tag_id = :tagId
        ORDER BY transactions.occurred_at DESC, transactions.created_at DESC, transactions.id ASC
        """,
    )
    fun observeTransactionsByTag(tagId: String): Flow<List<TransactionEntity>>
}
