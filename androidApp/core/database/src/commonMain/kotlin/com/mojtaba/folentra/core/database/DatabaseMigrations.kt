package com.mojtaba.folentra.core.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Room schema version 1 is the initial schema.
 *
 * Migration workflow for future schema changes:
 * 1. Change the Room entity or DAO schema surface.
 * 2. Bump [CURRENT_VERSION] and the `FolentraDatabase` `@Database` version together.
 * 3. Add an explicit `Migration(startVersion, endVersion)` object in this file.
 * 4. Add every released migration to [ALL] in version order.
 * 5. Run Room schema export and commit the new JSON schema snapshot under `core/database/schemas`.
 * 6. Add or update migration coverage for each version step and the full migration path.
 *
 * Keep every released migration in [ALL]. Tests intentionally fail when the database version is
 * bumped without registering the corresponding migration coverage.
 */
object DatabaseMigrations {
    const val INITIAL_VERSION: Int = 1

    const val CURRENT_VERSION: Int = 3

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_merchant` ON `transactions` (`merchant`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_note` ON `transactions` (`note`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_source` ON `transactions` (`source`)")
        }
    }

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("""
                CREATE TABLE IF NOT EXISTS `transaction_drafts` (
                    `id` TEXT NOT NULL, `amountInput` TEXT NOT NULL,
                    `currencyCode` TEXT NOT NULL, `type` TEXT NOT NULL,
                    `occurredAt` INTEGER, `categoryId` TEXT, `merchant` TEXT NOT NULL,
                    `note` TEXT NOT NULL, `source` TEXT NOT NULL, `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL, `confirmedTransactionId` TEXT, PRIMARY KEY(`id`)
                )
            """.trimIndent())
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
