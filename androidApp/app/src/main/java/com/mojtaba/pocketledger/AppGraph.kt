package com.mojtaba.pocketledger

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import androidx.work.WorkManager
import com.mojtaba.pocketledger.background.TaskWorkerRegistry
import com.mojtaba.pocketledger.background.WorkManagerScheduler
import com.mojtaba.pocketledger.core.background.BackgroundTaskScheduler
import com.mojtaba.pocketledger.core.data.repository.BudgetRepository
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalBudgetRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalCategoryRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalTagRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalTransactionRepository
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase

@Composable
fun rememberPocketLedgerAppGraph(): PocketLedgerAppGraph {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        PocketLedgerAppGraph.create(context)
    }
}

class PocketLedgerAppGraph private constructor(
    val transactionRepository: TransactionRepository,
    val budgetRepository: BudgetRepository,
    val categoryRepository: CategoryRepository,
    val tagRepository: TagRepository,
    val backgroundTaskScheduler: BackgroundTaskScheduler,
) {
    companion object {
        fun create(context: Context): PocketLedgerAppGraph {
            val database = Room.databaseBuilder(
                context,
                PocketLedgerDatabase::class.java,
                PocketLedgerDatabase.DATABASE_NAME,
            ).build()

            return PocketLedgerAppGraph(
                transactionRepository = LocalTransactionRepository(database.transactionDao()),
                budgetRepository = LocalBudgetRepository(database.budgetDao()),
                categoryRepository = LocalCategoryRepository(database.categoryDao()),
                tagRepository = LocalTagRepository(database.tagDao()),
                backgroundTaskScheduler = WorkManagerScheduler(
                    workManager = WorkManager.getInstance(context),
                    workerRegistry = TaskWorkerRegistry.Empty,
                ),
            )
        }
    }
}
