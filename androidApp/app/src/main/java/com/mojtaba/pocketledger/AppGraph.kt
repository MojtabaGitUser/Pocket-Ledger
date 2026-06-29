package com.mojtaba.pocketledger

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import androidx.work.WorkManager
import com.mojtaba.pocketledger.background.TaskWorkerRegistry
import com.mojtaba.pocketledger.background.WorkManagerScheduler
import com.mojtaba.pocketledger.core.ai.AiFallbackStrategy
import com.mojtaba.pocketledger.core.ai.AiProviderSelector
import com.mojtaba.pocketledger.core.ai.GeminiNanoAiProvider
import com.mojtaba.pocketledger.core.ai.MlKitAiProvider
import com.mojtaba.pocketledger.core.ai.NoOpAiProvider
import com.mojtaba.pocketledger.core.ai.RuleBasedAiProvider
import com.mojtaba.pocketledger.core.analytics.DebugProductAnalyticsLogger
import com.mojtaba.pocketledger.core.analytics.NoOpProductAnalyticsLogger
import com.mojtaba.pocketledger.core.analytics.ProductAnalyticsLogger
import com.mojtaba.pocketledger.core.analytics.ProductAnalyticsProviderState
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
import com.mojtaba.pocketledger.core.security.applock.AppLockManager
import com.mojtaba.pocketledger.core.security.logging.AppLogger
import com.mojtaba.pocketledger.core.security.logging.LoggingPolicy
import com.mojtaba.pocketledger.core.security.logging.SafeAppLogger
import com.mojtaba.pocketledger.core.security.preferences.EncryptedSensitivePreferences
import com.mojtaba.pocketledger.core.security.preferences.InMemorySensitivePreferences
import com.mojtaba.pocketledger.core.security.preferences.SensitivePreferences
import com.mojtaba.pocketledger.security.AndroidBiometricAppLockAuthenticator

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
    val aiProviderSelector: AiProviderSelector,
    val aiFallbackStrategy: AiFallbackStrategy,
    val sensitivePreferences: SensitivePreferences,
    val appLockManager: AppLockManager,
    @Suppress("unused")
    val backgroundTaskScheduler: BackgroundTaskScheduler,
    @Suppress("unused")
    val appLogger: AppLogger,
    @Suppress("unused")
    val productAnalyticsLogger: ProductAnalyticsLogger,
    val productAnalyticsProviderState: ProductAnalyticsProviderState,
) {
    companion object {
        fun createForTesting(
            transactionRepository: TransactionRepository,
            budgetRepository: BudgetRepository,
            categoryRepository: CategoryRepository,
            tagRepository: TagRepository,
            featureFlags: FeatureFlagEvaluator,
            aiProviderSelector: AiProviderSelector,
            aiFallbackStrategy: AiFallbackStrategy,
            sensitivePreferences: SensitivePreferences,
            appLockManager: AppLockManager,
            backgroundTaskScheduler: BackgroundTaskScheduler,
            appLogger: AppLogger,
            productAnalyticsLogger: ProductAnalyticsLogger = NoOpProductAnalyticsLogger(),
            productAnalyticsProviderState: ProductAnalyticsProviderState = ProductAnalyticsProviderState.NoOp,
        ): PocketLedgerAppGraph = PocketLedgerAppGraph(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            featureFlags = featureFlags,
            aiProviderSelector = aiProviderSelector,
            aiFallbackStrategy = aiFallbackStrategy,
            sensitivePreferences = sensitivePreferences,
            appLockManager = appLockManager,
            backgroundTaskScheduler = backgroundTaskScheduler,
            appLogger = appLogger,
            productAnalyticsLogger = productAnalyticsLogger,
            productAnalyticsProviderState = productAnalyticsProviderState,
        )

        fun create(
            context: Context,
            activityProvider: () -> FragmentActivity? = { null },
        ): PocketLedgerAppGraph {
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
            val appLogger = SafeAppLogger(policy = loggingPolicy)
            val analyticsProviderState = if (BuildConfig.LOGGING_ENABLED) {
                ProductAnalyticsProviderState.DebugSink
            } else {
                ProductAnalyticsProviderState.NoOp
            }
            val productAnalyticsLogger = if (BuildConfig.LOGGING_ENABLED) {
                DebugProductAnalyticsLogger { event ->
                    appLogger.debug("Product event logged name=${event.name} parameters=${event.parameters}")
                }
            } else {
                NoOpProductAnalyticsLogger()
            }
            val featureFlags = FeatureFlagEvaluator(LocalFeatureFlagProvider())
            val aiProviderSelector = AiProviderSelector(
                providers = listOf(
                    GeminiNanoAiProvider(),
                    MlKitAiProvider(),
                    RuleBasedAiProvider,
                    NoOpAiProvider,
                ),
                featureFlags = featureFlags,
            )
            val sensitivePreferences = if (BuildConfig.APP_ENV == "benchmark") {
                InMemorySensitivePreferences()
            } else {
                EncryptedSensitivePreferences(context)
            }

            return PocketLedgerAppGraph(
                transactionRepository = LocalTransactionRepository(database.transactionDao()),
                budgetRepository = LocalBudgetRepository(database.budgetDao()),
                categoryRepository = LocalCategoryRepository(database.categoryDao()),
                tagRepository = LocalTagRepository(database.tagDao()),
                featureFlags = featureFlags,
                aiProviderSelector = aiProviderSelector,
                aiFallbackStrategy = AiFallbackStrategy(aiProviderSelector),
                sensitivePreferences = sensitivePreferences,
                appLockManager = AppLockManager(
                    preferences = sensitivePreferences,
                    authenticator = AndroidBiometricAppLockAuthenticator(
                        biometricManager = BiometricManager.from(context),
                        activityProvider = activityProvider,
                    ),
                ),
                backgroundTaskScheduler = WorkManagerScheduler(
                    workManager = WorkManager.getInstance(context),
                    workerRegistry = TaskWorkerRegistry.Empty,
                ),
                appLogger = appLogger,
                productAnalyticsLogger = productAnalyticsLogger,
                productAnalyticsProviderState = analyticsProviderState,
            )
        }
    }
}