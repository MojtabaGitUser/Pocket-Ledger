package com.mojtaba.pocketledger.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mojtaba.pocketledger.core.database.model.TagEntity
import com.mojtaba.pocketledger.core.database.model.TransactionTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(tags: List<TagEntity>)

    @Upsert
    suspend fun upsert(tag: TagEntity)

    @Upsert
    suspend fun upsertAll(tags: List<TagEntity>)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: String): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :id")
    fun observeById(id: String): Flow<TagEntity?>

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC, id ASC")
    fun observeTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToTransaction(crossRef: TransactionTagCrossRef)

    @Query("DELETE FROM transaction_tags WHERE transaction_id = :transactionId AND tag_id = :tagId")
    suspend fun removeTagFromTransaction(transactionId: String, tagId: String): Int

    @Query(
        """
        SELECT tags.* FROM tags
        INNER JOIN transaction_tags ON tags.id = transaction_tags.tag_id
        WHERE transaction_tags.transaction_id = :transactionId
        ORDER BY tags.name COLLATE NOCASE ASC, tags.id ASC
        """,
    )
    fun observeTagsForTransaction(transactionId: String): Flow<List<TagEntity>>
}
