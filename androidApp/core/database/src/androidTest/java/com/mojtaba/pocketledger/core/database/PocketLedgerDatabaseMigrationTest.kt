package com.mojtaba.pocketledger.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun createVersion1Schema_containsExpectedTables() {
        helper.createDatabase(uniqueDatabaseName(), DatabaseMigrations.INITIAL_VERSION).use { database ->
            val tables = database.queryStringSet(
                """
                SELECT name FROM sqlite_master
                WHERE type = 'table' AND name NOT LIKE 'sqlite_%'
                """.trimIndent(),
            )

            assertTrue(tables.containsAll(EXPECTED_APP_TABLES))
        }
    }

    @Test
    fun createVersion1Schema_containsExpectedColumnsAndIndices() {
        helper.createDatabase(uniqueDatabaseName(), DatabaseMigrations.INITIAL_VERSION).use { database ->
            assertEquals(
                setOf(
                    "id",
                    "amount_minor",
                    "currency_code",
                    "type",
                    "occurred_at",
                    "category_id",
                    "merchant",
                    "note",
                    "source",
                    "is_recurring",
                    "created_at",
                    "updated_at",
                ),
                database.tableColumns("transactions"),
            )
            assertEquals(
                setOf(
                    "id",
                    "name",
                    "type",
                    "color_hex",
                    "icon_name",
                    "sort_order",
                    "is_active",
                    "created_at",
                    "updated_at",
                ),
                database.tableColumns("categories"),
            )
            assertEquals(
                setOf(
                    "id",
                    "name",
                    "amount_minor",
                    "currency_code",
                    "period_type",
                    "period_start",
                    "period_end",
                    "category_id",
                    "is_active",
                    "created_at",
                    "updated_at",
                ),
                database.tableColumns("budgets"),
            )
            assertEquals(
                setOf("id", "name", "color_hex", "created_at", "updated_at"),
                database.tableColumns("tags"),
            )
            assertEquals(
                setOf("transaction_id", "tag_id"),
                database.tableColumns("transaction_tags"),
            )

            database.assertHasIndex("transactions", listOf("category_id"))
            database.assertHasIndex("transactions", listOf("occurred_at"))
            database.assertHasIndex("transactions", listOf("type", "occurred_at"))
            database.assertHasIndex("transactions", listOf("category_id", "occurred_at"))
            database.assertHasIndex("categories", listOf("type", "is_active"))
            database.assertHasIndex("categories", listOf("name"))
            database.assertHasIndex("budgets", listOf("category_id"))
            database.assertHasIndex("budgets", listOf("is_active", "period_start", "period_end"))
            database.assertHasIndex("tags", listOf("name"))
            database.assertHasIndex("transaction_tags", listOf("transaction_id"))
            database.assertHasIndex("transaction_tags", listOf("tag_id"))
        }
    }

    @Test
    fun openLatestDatabase_withCurrentMigrations_succeeds() {
        val databaseName = uniqueDatabaseName()
        val context = ApplicationProvider.getApplicationContext<Context>()

        helper.createDatabase(databaseName, DatabaseMigrations.INITIAL_VERSION).close()

        val database = Room.databaseBuilder(context, PocketLedgerDatabase::class.java, databaseName)
            .addMigrations(*DatabaseMigrations.ALL)
            .allowMainThreadQueries()
            .build()

        try {
            database.openHelper.writableDatabase.query("SELECT * FROM transactions").close()
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun allMigrations_areRegisteredForCurrentVersion() {
        if (DatabaseMigrations.CURRENT_VERSION == DatabaseMigrations.INITIAL_VERSION) {
            assertTrue(DatabaseMigrations.ALL.isEmpty())
            return
        }

        val migrationPairs = DatabaseMigrations.ALL
            .map { it.startVersion to it.endVersion }
            .toSet()
        val expectedPairs = (DatabaseMigrations.INITIAL_VERSION until DatabaseMigrations.CURRENT_VERSION)
            .map { it to it + 1 }
            .toSet()

        assertEquals(expectedPairs, migrationPairs)
    }

    companion object {
        private val EXPECTED_APP_TABLES = setOf(
            "transactions",
            "categories",
            "budgets",
            "tags",
            "transaction_tags",
            "room_master_table",
        )

        private fun uniqueDatabaseName(): String = "pocket-ledger-migration-${UUID.randomUUID()}.db"
    }
}

private fun SupportSQLiteDatabase.queryStringSet(sql: String): Set<String> {
    val result = linkedSetOf<String>()
    query(sql).use { cursor ->
        while (cursor.moveToNext()) {
            result += cursor.getString(0)
        }
    }
    return result
}

private fun SupportSQLiteDatabase.tableColumns(tableName: String): Set<String> {
    val result = linkedSetOf<String>()
    query("PRAGMA table_info(`$tableName`)").use { cursor ->
        val nameIndex = cursor.getColumnIndexOrThrow("name")
        while (cursor.moveToNext()) {
            result += cursor.getString(nameIndex)
        }
    }
    return result
}

private fun SupportSQLiteDatabase.assertHasIndex(
    tableName: String,
    expectedColumns: List<String>,
) {
    val indexNames = queryStringColumnSet("PRAGMA index_list(`$tableName`)", "name")
    val hasIndex = indexNames.any { indexName ->
        queryIndexColumns(indexName) == expectedColumns
    }

    assertTrue(
        "Expected $tableName to have index on ${expectedColumns.joinToString()}",
        hasIndex,
    )
}

private fun SupportSQLiteDatabase.queryStringColumnSet(
    sql: String,
    columnName: String,
): Set<String> {
    val result = linkedSetOf<String>()
    query(sql).use { cursor ->
        val columnIndex = cursor.getColumnIndexOrThrow(columnName)
        while (cursor.moveToNext()) {
            result += cursor.getString(columnIndex)
        }
    }
    return result
}

private fun SupportSQLiteDatabase.queryIndexColumns(indexName: String): List<String> {
    val result = mutableListOf<String>()
    query("PRAGMA index_info(`$indexName`)").use { cursor ->
        val nameIndex = cursor.getColumnIndexOrThrow("name")
        while (cursor.moveToNext()) {
            result += cursor.getString(nameIndex)
        }
    }
    return result
}
