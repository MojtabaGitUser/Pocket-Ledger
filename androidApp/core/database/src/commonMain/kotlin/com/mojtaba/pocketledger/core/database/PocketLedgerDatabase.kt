package com.mojtaba.pocketledger.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Builder
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mojtaba.pocketledger.core.database.dao.BudgetDao
import com.mojtaba.pocketledger.core.database.dao.CategoryDao
import com.mojtaba.pocketledger.core.database.dao.TagDao
import com.mojtaba.pocketledger.core.database.dao.TransactionDao
import com.mojtaba.pocketledger.core.database.model.BudgetEntity
import com.mojtaba.pocketledger.core.database.model.CategoryEntity
import com.mojtaba.pocketledger.core.database.model.TagEntity
import com.mojtaba.pocketledger.core.database.model.TransactionEntity
import com.mojtaba.pocketledger.core.database.model.TransactionTagCrossRef
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
@ConstructedBy(PocketLedgerDatabaseConstructor::class)
abstract class PocketLedgerDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    abstract fun categoryDao(): CategoryDao

    abstract fun tagDao(): TagDao

    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "pocket-ledger.db"
    }
}

@Suppress("KotlinNoActualForExpect")
expect object PocketLedgerDatabaseConstructor : RoomDatabaseConstructor<PocketLedgerDatabase> {
    override fun initialize(): PocketLedgerDatabase
}

fun buildPocketLedgerDatabase(
    builder: Builder<PocketLedgerDatabase>,
    queryCoroutineContext: CoroutineContext = Dispatchers.IO,
): PocketLedgerDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryCoroutineContext)
        .addMigrations(*DatabaseMigrations.ALL)
        .build()
