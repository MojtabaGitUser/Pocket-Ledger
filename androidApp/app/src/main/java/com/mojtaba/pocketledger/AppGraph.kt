package com.mojtaba.pocketledger

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.work.WorkManager
import com.mojtaba.pocketledger.background.BackgroundJobSettingsManager
import com.mojtaba.pocketledger.background.MonthlySummaryPreparationWorker
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
import com.mojtaba.pocketledger.core.background.tasks.MonthlySummaryPreparationTask
import com.mojtaba.pocketledger.core.data.repository.BudgetRepository
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalBudgetRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalCategoryRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalTagRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalTransactionRepository
import com.mojtaba.pocketledger.core.database.createPocketLedgerDatabase
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.pocketledger.core.featureflags.InMemoryFeatureFlagOverrideStore
import com.mojtaba.pocketledger.core.featureflags.LocalFeatureFlagProvider
import com.mojtaba.pocketledger.core.featureflags.OverrideableFeatureFlagProvider
import com.mojtaba.pocketledger.core.featureflags.SharedPreferencesFeatureFlagOverrideStore
import com.mojtaba.pocketledger.core.security.applock.AppLockManager
import com.mojtaba.pocketledger.core.security.logging.AppLogger
import com.mojtaba.pocketledger.core.security.logging.LoggingPolicy
import com.mojtaba.pocketledger.core.security.logging.SafeAppLogger
import com.mojtaba.pocketledger.core.security.preferences.EncryptedSensitivePreferences
import com.mojtaba.pocketledger.core.security.preferences.InMemorySensitivePreferences
import com.mojtaba.pocketledger.core.security.preferences.SensitivePreferences
import com.mojtaba.pocketledger.observability.CrashReporter
import com.mojtaba.pocketledger.observability.CrashReporterFactory
import com.mojtaba.pocketledger.observability.CrashReportingStatus
import com.mojtaba.pocketledger.observability.DefaultStartupFailureReporter
import com.mojtaba.pocketledger.observability.NoOpCrashReporter
import com.mojtaba.pocketledger.observability.NoOpStartupFailureReporter
import com.mojtaba.pocketledger.observability.StartupFailureReporter
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
    val featureFlagProvider: OverrideableFeatureFlagProvider,
    val aiProviderSelector: AiProviderSelector,
    val aiFallbackStrategy: AiFallbackStrategy,
    val sensitivePreferences: SensitivePreferences,
    val appLockManager: AppLockManager,
    @Suppress("unused")
    val backgroundTaskScheduler: BackgroundTaskScheduler,
    val backgroundJobSettingsManager: BackgroundJobSettingsManager,
    @Suppress("unused")
    val appLogger: AppLogger,
    val crashReporter: CrashReporter,
    val startupFailureReporter: StartupFailureReporter,
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
            featureFlagProvider: OverrideableFeatureFlagProvider = OverrideableFeatureFlagProvider(),
            aiProviderSelector: AiProviderSelector,
            aiFallbackStrategy: AiFallbackStrategy,
            sensitivePreferences: SensitivePreferences,
            appLockManager: AppLockManager,
            backgroundTaskScheduler: BackgroundTaskScheduler,
            backgroundJobSettingsManager: BackgroundJobSettingsManager? = null,
            appLogger: AppLogger,
            crashReporter: CrashReporter = NoOpCrashReporter(
                CrashReportingStatus(provider = "No-op", configured = false, collectionEnabled = false),
            ),
            startupFailureReporter: StartupFailureReporter = NoOpStartupFailureReporter(),
            productAnalyticsLogger: ProductAnalyticsLogger = NoOpProductAnalyticsLogger(),
            productAnalyticsProviderState: ProductAnalyticsProviderState = ProductAnalyticsProviderState.NoOp,
        ): PocketLedgerAppGraph = PocketLedgerAppGraph(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            featureFlags = featureFlags,
            featureFlagProvider = featureFlagProvider,
            aiProviderSelector = aiProviderSelector,
            aiFallbackStrategy = aiFallbackStrategy,
            sensitivePreferences = sensitivePreferences,
            appLockManager = appLockManager,
            backgroundTaskScheduler = backgroundTaskScheduler,
            backgroundJobSettingsManager = backgroundJobSettingsManager ?: BackgroundJobSettingsManager(
                preferences = sensitivePreferences,
                scheduler = backgroundTaskScheduler,
                featureFlags = featureFlags,
            ),
            appLogger = appLogger,
            crashReporter = crashReporter,
            startupFailureReporter = startupFailureReporter,
            productAnalyticsLogger = productAnalyticsLogger,
            productAnalyticsProviderState = productAnalyticsProviderState,
        )

        fun create(
            context: Context,
            activityProvider: () -> FragmentActivity? = { null },
        ): PocketLedgerAppGraph {
            val loggingPolicy = if (BuildConfig.LOGGING_ENABLED) {
                LoggingPolicy.Debug
            } else {
                LoggingPolicy.Release
            }
            val appLogger = SafeAppLogger(policy = loggingPolicy)
            val crashReporter = CrashReporterFactory.create(
                context = context,
                collectionEnabled = BuildConfig.CRASH_REPORTING_ENABLED,
            )
            val startupFailureReporter = DefaultStartupFailureReporter(
                crashReporter = crashReporter,
                appLogger = appLogger,
            )

            return runCatching {
                val database = createPocketLedgerDatabase(context)
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
                val featureFlagProvider = OverrideableFeatureFlagProvider(
                    baseProvider = LocalFeatureFlagProvider(),
                    overrideStore = if (BuildConfig.APP_ENV == "debug") {
                        SharedPreferencesFeatureFlagOverrideStore(
                            context.getSharedPreferences(
                                "pocket_ledger_feature_flag_overrides",
                                Context.MODE_PRIVATE,
                            ),
                        )
                    } else {
                        InMemoryFeatureFlagOverrideStore()
                    },
                )
                val featureFlags = FeatureFlagEvaluator(featureFlagProvider)
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

                val backgroundTaskScheduler = WorkManagerScheduler(
                    workManager = WorkManager.getInstance(context),
                    workerRegistry = TaskWorkerRegistry(
                        mapOf(MonthlySummaryPreparationTask.Id to MonthlySummaryPreparationWorker::class.java),
                    ),
                )

                PocketLedgerAppGraph(
                    transactionRepository = LocalTransactionRepository(database.transactionDao()),
                    budgetRepository = LocalBudgetRepository(database.budgetDao()),
                    categoryRepository = LocalCategoryRepository(database.categoryDao()),
                    tagRepository = LocalTagRepository(database.tagDao()),
                    featureFlags = featureFlags,
                    featureFlagProvider = featureFlagProvider,
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
                    backgroundTaskScheduler = backgroundTaskScheduler,
                    backgroundJobSettingsManager = BackgroundJobSettingsManager(
                        preferences = sensitivePreferences,
                        scheduler = backgroundTaskScheduler,
                        featureFlags = featureFlags,
                    ),
                    appLogger = appLogger,
                    crashReporter = crashReporter,
                    startupFailureReporter = startupFailureReporter,
                    productAnalyticsLogger = productAnalyticsLogger,
                    productAnalyticsProviderState = analyticsProviderState,
                )
            }.getOrElse { throwable ->
                startupFailureReporter.recordCriticalFailure(
                    throwable = throwable,
                    stage = "app_graph_create",
                )
                throw throwable
            }
        }
    }
}
