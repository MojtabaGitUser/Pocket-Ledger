package com.mojtaba.pocketledger.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getPocketLedgerDatabaseBuilder(context: Context): RoomDatabase.Builder<PocketLedgerDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(PocketLedgerDatabase.DATABASE_NAME)
    return Room.databaseBuilder<PocketLedgerDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}

fun createPocketLedgerDatabase(context: Context): PocketLedgerDatabase =
    buildPocketLedgerDatabase(getPocketLedgerDatabaseBuilder(context))
