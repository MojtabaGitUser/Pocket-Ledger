package com.mojtaba.folentra.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getFolentraDatabaseBuilder(
    databasePath: String = defaultDesktopDatabasePath(),
): RoomDatabase.Builder<FolentraDatabase> =
    Room.databaseBuilder<FolentraDatabase>(name = databasePath)

fun createFolentraDatabase(
    databasePath: String = defaultDesktopDatabasePath(),
): FolentraDatabase =
    buildFolentraDatabase(getFolentraDatabaseBuilder(databasePath))

fun defaultDesktopDatabasePath(): String {
    val appDataDirectory = File(System.getProperty("user.home"), ".folentra")
    return File(appDataDirectory, FolentraDatabase.DATABASE_NAME).absolutePath
}
