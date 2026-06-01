package com.mojtaba.pocketledger.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PocketLedgerDatabaseMigrationTest {
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PocketLedgerDatabase::class.java,
    )

    @Test
    fun createVersion1Schema_succeeds() {
        helper.createDatabase(TEST_DATABASE_NAME, 1).use { database ->
            database.execSQL("SELECT * FROM transactions")
            database.execSQL("SELECT * FROM categories")
            database.execSQL("SELECT * FROM budgets")
            database.execSQL("SELECT * FROM tags")
            database.execSQL("SELECT * FROM transaction_tags")
        }
    }

    companion object {
        private const val TEST_DATABASE_NAME = "pocket-ledger-migration-test"
    }
}
