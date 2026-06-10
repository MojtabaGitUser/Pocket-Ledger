package com.mojtaba.pocketledger.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseMigrationsTest {
    @Test
    fun initialVersion_isVersionOne() {
        assertEquals(1, DatabaseMigrations.INITIAL_VERSION)
    }

    @Test
    fun currentVersion_isVersionTwo() {
        assertEquals(2, DatabaseMigrations.CURRENT_VERSION)
    }

    @Test
    fun migrationOneToTwo_isRegistered() {
        assertEquals(
            listOf(1 to 2),
            DatabaseMigrations.ALL.map { it.startVersion to it.endVersion },
        )
    }

    @Test
    fun migrations_doNotContainDuplicateVersionPairs() {
        val migrationPairs = DatabaseMigrations.ALL.map { it.startVersion to it.endVersion }

        assertEquals(migrationPairs.toSet().size, migrationPairs.size)
    }

    @Test
    fun migrationsMayBeEmpty_onlyForInitialSchemaVersion() {
        if (DatabaseMigrations.ALL.isEmpty()) {
            assertEquals(DatabaseMigrations.INITIAL_VERSION, DatabaseMigrations.CURRENT_VERSION)
        } else {
            assertTrue(DatabaseMigrations.CURRENT_VERSION > DatabaseMigrations.INITIAL_VERSION)
        }
    }
}
