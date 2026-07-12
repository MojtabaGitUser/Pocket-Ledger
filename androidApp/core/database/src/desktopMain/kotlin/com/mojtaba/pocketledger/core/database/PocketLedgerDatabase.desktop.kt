package com.mojtaba.pocketledger.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getPocketLedgerDatabaseBuilder(
    databasePath: String = defaultDesktopDatabasePath(),
): RoomDatabase.Builder<PocketLedgerDatabase> =
    Room.databaseBuilder<PocketLedgerDatabase>(name = databasePath)

fun createPocketLedgerDatabase(
    databasePath: String = defaultDesktopDatabasePath(),
): PocketLedgerDatabase =
    buildPocketLedgerDatabase(getPocketLedgerDatabaseBuilder(databasePath))

fun defaultDesktopDatabasePath(): String {
    val appDataDirectory = File(System.getProperty("user.home"), ".pocket-ledger")
    return File(appDataDirectory, PocketLedgerDatabase.DATABASE_NAME).absolutePath
}
