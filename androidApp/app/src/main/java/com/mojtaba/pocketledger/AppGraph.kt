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
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.pocketledger.core.featureflags.LocalFeatureFlagProvider
import com.mojtaba.pocketledger.core.security.logging.AppLogger
import com.mojtaba.pocketledger.core.security.logging.LoggingPolicy
import com.mojtaba.pocketledger.core.security.logging.SafeAppLogger

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
    val featureFlags: FeatureFlagEvaluator,
    val backgroundTaskScheduler: BackgroundTaskScheduler,
    val appLogger: AppLogger,
) {
    companion object {
        fun createForTesting(
            transactionRepository: TransactionRepository,
            budgetRepository: BudgetRepository,
            categoryRepository: CategoryRepository,
            tagRepository: TagRepository,
            featureFlags: FeatureFlagEvaluator,
            backgroundTaskScheduler: BackgroundTaskScheduler,
            appLogger: AppLogger,
        ): PocketLedgerAppGraph = PocketLedgerAppGraph(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            featureFlags = featureFlags,
            backgroundTaskScheduler = backgroundTaskScheduler,
            appLogger = appLogger,
        )

        fun create(context: Context): PocketLedgerAppGraph {
            val database = Room.databaseBuilder(
                context,
                PocketLedgerDatabase::class.java,
                PocketLedgerDatabase.DATABASE_NAME,
            ).build()
            val loggingPolicy = if (BuildConfig.LOGGING_ENABLED) {
                LoggingPolicy.Debug
            } else {
                LoggingPolicy.Release
            }

            return PocketLedgerAppGraph(
                transactionRepository = LocalTransactionRepository(database.transactionDao()),
                budgetRepository = LocalBudgetRepository(database.budgetDao()),
                categoryRepository = LocalCategoryRepository(database.categoryDao()),
                tagRepository = LocalTagRepository(database.tagDao()),
                featureFlags = FeatureFlagEvaluator(LocalFeatureFlagProvider()),
                backgroundTaskScheduler = WorkManagerScheduler(
                    workManager = WorkManager.getInstance(context),
                    workerRegistry = TaskWorkerRegistry.Empty,
                ),
                appLogger = SafeAppLogger(policy = loggingPolicy),
            )
        }
    }
}
