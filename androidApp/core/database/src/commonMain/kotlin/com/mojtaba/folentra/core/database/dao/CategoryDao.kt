package com.mojtaba.folentra.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.mojtaba.folentra.core.database.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE id = :id")
    fun observeById(id: String): Flow<CategoryEntity?>

    @Query("SELECT * FROM categories ORDER BY sort_order ASC, name COLLATE NOCASE ASC, id ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT * FROM categories
        WHERE is_active = 1
        ORDER BY sort_order ASC, name COLLATE NOCASE ASC, id ASC
        """,
    )
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT * FROM categories
        WHERE type = :type AND is_active = 1
        ORDER BY sort_order ASC, name COLLATE NOCASE ASC, id ASC
        """,
    )
    fun observeActiveCategoriesByType(type: String): Flow<List<CategoryEntity>>
}
