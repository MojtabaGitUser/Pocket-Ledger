package com.mojtaba.folentra

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.work.WorkManager
import com.mojtaba.folentra.background.BackgroundJobSettingsManager
import com.mojtaba.folentra.background.MonthlySummaryPreparationWorker
import com.mojtaba.folentra.background.TaskWorkerRegistry
import com.mojtaba.folentra.account.AndroidCredentialManagerPasskeyClient
import com.mojtaba.folentra.account.AndroidPlayIntegrityRequestHook
import com.mojtaba.folentra.background.WorkManagerScheduler
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.ai.AiProviderSelector
import com.mojtaba.folentra.core.ai.GeminiNanoAiProvider
import com.mojtaba.folentra.core.ai.MlKitAiProvider
import com.mojtaba.folentra.core.ai.NoOpAiProvider
import com.mojtaba.folentra.core.ai.RuleBasedAiProvider
import com.mojtaba.folentra.core.analytics.DebugProductAnalyticsLogger
import com.mojtaba.folentra.core.analytics.NoOpProductAnalyticsLogger
import com.mojtaba.folentra.core.analytics.ProductAnalyticsLogger
import com.mojtaba.folentra.core.analytics.ProductAnalyticsProviderState
import com.mojtaba.folentra.core.background.BackgroundTaskScheduler
import com.mojtaba.folentra.core.background.tasks.MonthlySummaryPreparationTask
import com.mojtaba.folentra.core.data.repository.BudgetRepository
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TagRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.core.data.repository.local.LocalBudgetRepository
import com.mojtaba.folentra.core.data.repository.local.LocalCategoryRepository
import com.mojtaba.folentra.core.data.repository.local.LocalTagRepository
import com.mojtaba.folentra.core.data.repository.local.LocalTransactionRepository
import com.mojtaba.folentra.core.database.createFolentraDatabase
import com.mojtaba.folentra.core.featureflags.DefaultFeatureFlags
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.folentra.core.featureflags.InMemoryFeatureFlagOverrideStore
import com.mojtaba.folentra.core.featureflags.LocalFeatureFlagProvider
import com.mojtaba.folentra.core.featureflags.OverrideableFeatureFlagProvider
import com.mojtaba.folentra.core.featureflags.SharedPreferencesFeatureFlagOverrideStore
import com.mojtaba.folentra.core.security.applock.AppLockManager
import com.mojtaba.folentra.core.security.backup.BackupReadyProfileManager
import com.mojtaba.folentra.core.security.backup.BackupReadyProfilePrerequisites
import com.mojtaba.folentra.core.security.integrity.PlayIntegrityRequestHook
import com.mojtaba.folentra.core.security.integrity.NoOpPlayIntegrityRequestHook
import com.mojtaba.folentra.core.security.passkey.NoOpPasskeyBackendContract
import com.mojtaba.folentra.core.security.passkey.NoOpPasskeyClient
import com.mojtaba.folentra.core.security.passkey.PasskeyBackendContract
import com.mojtaba.folentra.core.security.passkey.PasskeyClient
import com.mojtaba.folentra.core.security.logging.AppLogger
import com.mojtaba.folentra.core.security.logging.LoggingPolicy
import com.mojtaba.folentra.core.security.logging.SafeAppLogger
import com.mojtaba.folentra.core.security.preferences.DefaultSensitivePreferenceKeys
import com.mojtaba.folentra.core.security.preferences.EncryptedSensitivePreferences
import com.mojtaba.folentra.core.security.preferences.InMemorySensitivePreferences
import com.mojtaba.folentra.core.security.preferences.SensitivePreferences
import com.mojtaba.folentra.observability.CrashReporter
import com.mojtaba.folentra.observability.CrashReporterFactory
import com.mojtaba.folentra.observability.CrashReportingStatus
import com.mojtaba.folentra.observability.DefaultStartupFailureReporter
import com.mojtaba.folentra.observability.NoOpCrashReporter
import com.mojtaba.folentra.observability.NoOpStartupFailureReporter
import com.mojtaba.folentra.observability.StartupFailureReporter
import com.mojtaba.folentra.security.AndroidBiometricAppLockAuthenticator

@Composable
fun rememberFolentraAppGraph(): FolentraAppGraph {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        FolentraAppGraph.create(context)
    }
}

class FolentraAppGraph private constructor(
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
    val passkeyClient: PasskeyClient,
    val passkeyBackendContract: PasskeyBackendContract,
    val playIntegrityRequestHook: PlayIntegrityRequestHook,
    val backupReadyProfileManager: BackupReadyProfileManager,
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
            passkeyClient: PasskeyClient = NoOpPasskeyClient(),
            passkeyBackendContract: PasskeyBackendContract = NoOpPasskeyBackendContract(),
            playIntegrityRequestHook: PlayIntegrityRequestHook = NoOpPlayIntegrityRequestHook(),
            backupReadyProfileManager: BackupReadyProfileManager = BackupReadyProfileManager(sensitivePreferences),
            backgroundTaskScheduler: BackgroundTaskScheduler,
            backgroundJobSettingsManager: BackgroundJobSettingsManager? = null,
            appLogger: AppLogger,
            crashReporter: CrashReporter = NoOpCrashReporter(
                CrashReportingStatus(provider = "No-op", configured = false, collectionEnabled = false),
            ),
            startupFailureReporter: StartupFailureReporter = NoOpStartupFailureReporter(),
            productAnalyticsLogger: ProductAnalyticsLogger = NoOpProductAnalyticsLogger(),
            productAnalyticsProviderState: ProductAnalyticsProviderState = ProductAnalyticsProviderState.NoOp,
        ): FolentraAppGraph = FolentraAppGraph(
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
            passkeyClient = passkeyClient,
            passkeyBackendContract = passkeyBackendContract,
            playIntegrityRequestHook = playIntegrityRequestHook,
            backupReadyProfileManager = backupReadyProfileManager,
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
        ): FolentraAppGraph {
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
                val database = createFolentraDatabase(context)
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
                                "folentra_feature_flag_overrides",
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

                val passkeyClient = AndroidCredentialManagerPasskeyClient(context)
                val playIntegrityRequestHook = AndroidPlayIntegrityRequestHook(context)
                val backupReadyProfileManager = BackupReadyProfileManager(
                    preferences = sensitivePreferences,
                    prerequisitesProvider = { preferences ->
                        BackupReadyProfilePrerequisites(
                            passkeyAccountFlowEnabled = featureFlags.isEnabled(DefaultFeatureFlags.PasskeyAccountFlowEnabled),
                            cloudSyncEnabled = featureFlags.isEnabled(DefaultFeatureFlags.CloudSyncEnabled),
                            passkeyCredentialStored = !preferences.getString(DefaultSensitivePreferenceKeys.PasskeyCredentialId).isNullOrBlank(),
                            accountSessionStored = !preferences.getString(DefaultSensitivePreferenceKeys.AccountSessionToken).isNullOrBlank(),
                        )
                    },
                )

                val backgroundTaskScheduler = WorkManagerScheduler(
                    workManager = WorkManager.getInstance(context),
                    workerRegistry = TaskWorkerRegistry(
                        mapOf(MonthlySummaryPreparationTask.Id to MonthlySummaryPreparationWorker::class.java),
                    ),
                )

                FolentraAppGraph(
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
                    passkeyClient = passkeyClient,
                    passkeyBackendContract = NoOpPasskeyBackendContract(),
                    playIntegrityRequestHook = playIntegrityRequestHook,
                    backupReadyProfileManager = backupReadyProfileManager,
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
