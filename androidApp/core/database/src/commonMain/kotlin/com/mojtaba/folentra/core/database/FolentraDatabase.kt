package com.mojtaba.folentra.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Builder
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mojtaba.folentra.core.database.dao.BudgetDao
import com.mojtaba.folentra.core.database.dao.CategoryDao
import com.mojtaba.folentra.core.database.dao.TagDao
import com.mojtaba.folentra.core.database.dao.TransactionDao
import com.mojtaba.folentra.core.database.model.BudgetEntity
import com.mojtaba.folentra.core.database.model.CategoryEntity
import com.mojtaba.folentra.core.database.model.TagEntity
import com.mojtaba.folentra.core.database.model.TransactionEntity
import com.mojtaba.folentra.core.database.model.TransactionTagCrossRef
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

@Database(
    entities = [
        BudgetEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        TransactionEntity::class,
        TransactionTagCrossRef::class,
    ],
    version = DatabaseMigrations.CURRENT_VERSION,
    exportSchema = true,
)
@ConstructedBy(FolentraDatabaseConstructor::class)
abstract class FolentraDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    abstract fun categoryDao(): CategoryDao

    abstract fun tagDao(): TagDao

    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "folentra.db"
    }
}

@Suppress("KotlinNoActualForExpect")
expect object FolentraDatabaseConstructor : RoomDatabaseConstructor<FolentraDatabase> {
    override fun initialize(): FolentraDatabase
}

fun buildFolentraDatabase(
    builder: Builder<FolentraDatabase>,
    queryCoroutineContext: CoroutineContext = Dispatchers.IO,
): FolentraDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryCoroutineContext)
        .addMigrations(*DatabaseMigrations.ALL)
        .build()
