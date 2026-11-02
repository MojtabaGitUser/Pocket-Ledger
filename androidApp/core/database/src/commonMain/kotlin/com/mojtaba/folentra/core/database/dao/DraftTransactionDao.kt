package com.mojtaba.folentra.core.database.dao

import androidx.room.*
import com.mojtaba.folentra.core.database.model.DraftTransactionEntity
import com.mojtaba.folentra.core.database.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DraftTransactionDao {
    @Query("SELECT * FROM transaction_drafts WHERE confirmedTransactionId IS NULL ORDER BY updatedAt DESC, id ASC")
    abstract fun observePending(): Flow<List<DraftTransactionEntity>>

    @Query("SELECT * FROM transaction_drafts WHERE id = :id")
    abstract suspend fun getById(id: String): DraftTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(draft: DraftTransactionEntity)

    @Update
    protected abstract suspend fun update(draft: DraftTransactionEntity)

    @Query("DELETE FROM transaction_drafts WHERE id = :id AND confirmedTransactionId IS NULL")
    abstract suspend fun discard(id: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertTransaction(transaction: TransactionEntity)

    @Transaction
    open suspend fun savePending(draft: DraftTransactionEntity) {
        require(draft.confirmedTransactionId == null)
        val existing = getById(draft.id)
        if (existing == null) insert(draft)
        else {
            check(existing.confirmedTransactionId == null) { "Draft is already confirmed" }
            update(draft.copy(createdAt = existing.createdAt, source = existing.source))
        }
    }

    /** Both writes commit together. A retry returns the original ID, even after ledger deletion. */
    @Transaction
    open suspend fun confirm(draft: DraftTransactionEntity, transaction: TransactionEntity): String {
        val existing = checkNotNull(getById(draft.id)) { "Draft no longer exists" }
        existing.confirmedTransactionId?.let { return it }
        require(transaction.id == "draft:${draft.id}")
        insertTransaction(transaction.copy(source = existing.source))
        // Keep only the confirmation marker; don't retain a hidden copy of financial details.
        update(draft.copy(createdAt = existing.createdAt, source = existing.source,
            amountInput = "", currencyCode = "", type = "", occurredAt = null,
            categoryId = null, merchant = "", note = "", confirmedTransactionId = transaction.id))
        return transaction.id
    }
}
