package com.mojtaba.folentra.core.database

import com.mojtaba.folentra.core.database.model.CategoryEntity
import com.mojtaba.folentra.core.database.model.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FolentraDesktopDatabaseTest {
    @Test
    fun desktopDatabase_persistsRowsAcrossInstances() = runTest {
        val dbFile = File.createTempFile("folentra-desktop", ".db").apply { delete() }
        try {
            val firstDatabase = createFolentraDatabase(dbFile.absolutePath)
            try {
                val now = 1_770_000_000_000L
                firstDatabase.categoryDao().insert(
                    CategoryEntity(
                        id = "desktop-test-category",
                        name = "Desktop Test",
                        type = "expense",
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
                firstDatabase.transactionDao().insert(
                    TransactionEntity(
                        id = "desktop-test-transaction",
                        amountMinor = -1234,
                        currencyCode = "USD",
                        type = "expense",
                        occurredAt = now,
                        categoryId = "desktop-test-category",
                        merchant = "Desktop Store",
                        note = "Persistence check",
                        source = "desktop-test",
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            } finally {
                firstDatabase.close()
            }

            val secondDatabase = createFolentraDatabase(dbFile.absolutePath)
            try {
                val restored = secondDatabase.transactionDao().observeRecentTransactions(10).first()

                assertEquals(1, restored.size)
                assertEquals("desktop-test-transaction", restored.single().id)
                assertNotNull(secondDatabase.categoryDao().getById("desktop-test-category"))
            } finally {
                secondDatabase.close()
            }
        } finally {
            dbFile.delete()
            File(dbFile.absolutePath + "-shm").delete()
            File(dbFile.absolutePath + "-wal").delete()
        }
    }
}
