package com.mojtaba.pocketledger.core.database

import androidx.room.migration.Migration

/**
 * Room schema version 1 is the initial schema. Future migrations should start at 1 -> 2.
 *
 * Migration workflow for future schema changes:
 * 1. Change the Room entity or DAO schema surface.
 * 2. Bump [CURRENT_VERSION] and the `PocketLedgerDatabase` `@Database` version together.
 * 3. Add an explicit `Migration(startVersion, endVersion)` object in this file.
 * 4. Add every released migration to [ALL] in version order.
 * 5. Run Room schema export and commit the new JSON schema snapshot under `core/database/schemas`.
 * 6. Add or update `MigrationTestHelper` coverage for each version step and the full migration path.
 *
 * Keep every released migration in [ALL]. Tests intentionally fail when the database version is
 * bumped without registering the corresponding migration coverage.
 */
object DatabaseMigrations {
    const val INITIAL_VERSION: Int = 1

    const val CURRENT_VERSION: Int = 1

    val ALL: Array<Migration> = emptyArray()
}
