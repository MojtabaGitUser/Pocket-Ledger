package com.mojtaba.pocketledger.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mojtaba.pocketledger.core.database.dao.BudgetDao
import com.mojtaba.pocketledger.core.database.dao.CategoryDao
import com.mojtaba.pocketledger.core.database.dao.TagDao
import com.mojtaba.pocketledger.core.database.dao.TransactionDao
import com.mojtaba.pocketledger.core.database.model.BudgetEntity
import com.mojtaba.pocketledger.core.database.model.CategoryEntity
import com.mojtaba.pocketledger.core.database.model.TagEntity
import com.mojtaba.pocketledger.core.database.model.TransactionEntity
import com.mojtaba.pocketledger.core.database.model.TransactionTagCrossRef

@Database(
    entities = [
        BudgetEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        TransactionEntity::class,
        TransactionTagCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PocketLedgerDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    abstract fun categoryDao(): CategoryDao

    abstract fun tagDao(): TagDao

    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "pocket-ledger.db"
    }
}
