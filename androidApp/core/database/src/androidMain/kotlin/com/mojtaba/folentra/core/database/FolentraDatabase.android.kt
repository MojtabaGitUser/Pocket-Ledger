package com.mojtaba.folentra.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getFolentraDatabaseBuilder(context: Context): RoomDatabase.Builder<FolentraDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(FolentraDatabase.DATABASE_NAME)
    return Room.databaseBuilder<FolentraDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}

fun createFolentraDatabase(context: Context): FolentraDatabase =
    buildFolentraDatabase(getFolentraDatabaseBuilder(context))
