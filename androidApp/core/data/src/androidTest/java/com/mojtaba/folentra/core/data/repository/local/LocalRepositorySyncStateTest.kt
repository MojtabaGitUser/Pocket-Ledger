package com.mojtaba.folentra.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.data.repository.contract.OfflineFirstRepository
import com.mojtaba.folentra.core.data.repository.contract.SyncStatus
import com.mojtaba.folentra.core.database.FolentraDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LocalRepositorySyncStateTest {
    private lateinit var database: FolentraDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FolentraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun localRepositoriesExposeLocalOnlySyncState() = runTest {
        val repositories = listOf<OfflineFirstRepository>(
            LocalTransactionRepository(database.transactionDao()),
            LocalCategoryRepository(database.categoryDao()),
            LocalBudgetRepository(database.budgetDao()),
            LocalTagRepository(database.tagDao()),
        )

        repositories.forEach { repository ->
            val syncState = repository.observeSyncState().first()

            assertEquals(repository.repositoryName, SyncStatus.LOCAL_ONLY, syncState.status)
            assertEquals(repository.repositoryName, 0, syncState.pendingLocalChanges)
            assertNull(repository.repositoryName, syncState.lastSyncedAt)
            assertNull(repository.repositoryName, syncState.errorMessage)
        }
    }
}
