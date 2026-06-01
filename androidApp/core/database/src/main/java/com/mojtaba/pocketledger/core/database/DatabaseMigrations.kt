package com.mojtaba.pocketledger.core.database

import androidx.room.migration.Migration

/**
 * Room schema version 1 is the initial schema. Future migrations should start at 1 -> 2.
 *
 * Keep every released migration in this array and cover it with MigrationTestHelper tests.
 */
object DatabaseMigrations {
    val ALL: Array<Migration> = emptyArray()
}
