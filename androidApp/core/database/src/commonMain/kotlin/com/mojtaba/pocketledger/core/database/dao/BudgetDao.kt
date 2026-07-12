package com.mojtaba.pocketledger.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.mojtaba.pocketledger.core.database.model.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Upsert
    suspend fun upsertAll(budgets: List<BudgetEntity>)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun observeById(id: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets ORDER BY is_active DESC, period_start DESC, name COLLATE NOCASE ASC, id ASC")
    fun observeBudgets(): Flow<List<BudgetEntity>>

    @Query(
        """
        SELECT * FROM budgets
        WHERE is_active = 1
        ORDER BY period_start DESC, name COLLATE NOCASE ASC, id ASC
        """,
    )
    fun observeActiveBudgets(): Flow<List<BudgetEntity>>

    @Query(
        """
        SELECT * FROM budgets
        WHERE category_id = :categoryId
        ORDER BY is_active DESC, period_start DESC, name COLLATE NOCASE ASC, id ASC
        """,
    )
    fun observeBudgetsByCategory(categoryId: String): Flow<List<BudgetEntity>>

    @Query(
        """
        SELECT * FROM budgets
        WHERE period_start <= :endInclusive AND period_end >= :startInclusive
        ORDER BY period_start DESC, name COLLATE NOCASE ASC, id ASC
        """,
    )
    fun observeBudgetsByPeriodRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<BudgetEntity>>
}
