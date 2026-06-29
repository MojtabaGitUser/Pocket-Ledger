package com.mojtaba.pocketledger.debughealth

import android.content.Context
import android.content.pm.ApplicationInfo
import com.mojtaba.pocketledger.BuildConfig
import com.mojtaba.pocketledger.core.database.DatabaseMigrations
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import com.mojtaba.pocketledger.core.featureflags.BooleanFeatureFlag
import com.mojtaba.pocketledger.core.featureflags.DefaultFeatureFlags
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator

class DebugHealthReportFactory(
    private val buildInfo: DebugHealthBuildInfo,
    private val featureFlagStates: List<DebugHealthStatus>,
) {
    fun create(): DebugHealthReport =
        DebugHealthReport(
            sections = listOf(
                buildSection(),
                cicdSection(),
                testingSection(),
                firebaseSection(),
                observabilitySection(),
                databaseSection(),
                featureFlagSection(),
                releaseSafetySection(),
            ),
        )

    private fun buildSection(): DebugHealthSection =
        DebugHealthSection(
            title = "Build",
            statuses = listOf(
                status("App name", buildInfo.appName, "Application label from the installed package."),
                status(
                    "Version",
                    "${buildInfo.versionName} (${buildInfo.versionCode})",
                    "Version name and version code from BuildConfig.",
                ),
                status("Application ID", buildInfo.applicationId, "Runtime application ID for this build."),
                status("Build type", buildInfo.buildType, "Gradle build type."),
                status("Flavor", buildInfo.flavor.ifBlank { "None" }, "Gradle product flavor."),
                status("Environment", buildInfo.appEnvironment, "Safe app environment label from BuildConfig."),
                status(
                    "Debuggable",
                    enabledValue(buildInfo.debuggable),
                    "Whether the runtime package is debuggable.",
                    if (buildInfo.debuggable) DebugHealthSeverity.Ready else DebugHealthSeverity.Warning,
                ),
                status(
                    "Internal diagnostics",
                    if (buildInfo.internalBuild) "Debug only" else "Release hidden",
                    "Debug health diagnostics are gated by internal build configuration.",
                    if (buildInfo.internalBuild) DebugHealthSeverity.Ready else DebugHealthSeverity.Warning,
                ),
            ),
        )

    private fun cicdSection(): DebugHealthSection =
        DebugHealthSection(
            title = "CI/CD",
            statuses = listOf(
                status(
                    "CI environment",
                    if (buildInfo.ci) "Detected" else "Unavailable in local build",
                    "CI is detected only when a safe CI flag is present at runtime.",
                ),
                status(
                    "PR validation",
                    "Configured via CI",
                    "GitHub Actions run lint, JVM/shared tests, debug assembly, release assembly, and benchmark assembly.",
                    DebugHealthSeverity.Ready,
                ),
                status(
                    "Unsafe merge blocking",
                    "Configured via branch protection",
                    "CI failures are expected to block unsafe merges when required checks are enabled in GitHub.",
                ),
                status(
                    "Release candidate workflow",
                    "Configured via CI",
                    "Release candidate workflow builds release APK/AAB artifacts and benchmark artifacts.",
                    DebugHealthSeverity.Ready,
                ),
            ),
        )

    private fun testingSection(): DebugHealthSection =
        DebugHealthSection(
            title = "Testing",
            statuses = listOf(
                status(
                    "Unit tests",
                    "Configured via CI",
                    "PR validation runs testDebugUnitTest and shared allTests.",
                    DebugHealthSeverity.Ready,
                ),
                status(
                    "Lint",
                    "Configured via CI",
                    "PR validation runs lintDebug.",
                    DebugHealthSeverity.Ready,
                ),
                status(
                    "Benchmark artifacts",
                    "Configured via CI",
                    "PR, scheduled, and manual workflows assemble benchmark APKs.",
                    DebugHealthSeverity.Ready,
                ),
                status(
                    "Connected benchmarks",
                    "Manual device required",
                    "Connected Macrobenchmark execution is intentionally manual because it requires stable device infrastructure.",
                ),
                status(
                    "Screenshot workflow",
                    "Configured via CI",
                    "Paparazzi adaptive screenshot validation runs on schedule and manual dispatch.",
                    DebugHealthSeverity.Ready,
                ),
            ),
        )

    private fun firebaseSection(): DebugHealthSection =
        DebugHealthSection(
            title = "Firebase/App Distribution",
            statuses = listOf(
                status(
                    "Firebase config",
                    if (buildInfo.firebaseConfigured) "Detected" else "Not configured",
                    "Firebase app resource presence only; no keys or app identifiers are displayed.",
                    if (buildInfo.firebaseConfigured) DebugHealthSeverity.Ready else DebugHealthSeverity.Warning,
                ),
                status(
                    "Analytics dependency",
                    "Configured",
                    "Firebase Analytics is included through the Firebase BoM.",
                    DebugHealthSeverity.Ready,
                ),
                status(
                    "Crash reporting",
                    "Not configured",
                    "Crashlytics runtime dependency is not present in the app module.",
                    DebugHealthSeverity.Warning,
                ),
                status(
                    "App Distribution",
                    "Configured via CI",
                    "Internal debug APK distribution is handled by GitHub Actions and Firebase CLI, not app runtime code.",
                    DebugHealthSeverity.Ready,
                ),
            ),
        )

    private fun observabilitySection(): DebugHealthSection =
        DebugHealthSection(
            title = "Observability",
            statuses = listOf(
                status(
                    "Logging mode",
                    if (buildInfo.loggingEnabled) "Debug logging sanitized" else "Release-safe logging",
                    "SafeAppLogger redacts sensitive values and release policy suppresses debug/info logs.",
                    if (buildInfo.loggingEnabled) DebugHealthSeverity.Ready else DebugHealthSeverity.Neutral,
                ),
                status(
                    "Startup health",
                    "No critical startup failure recorded",
                    "No dedicated startup failure tracker exists yet; this screen reports the safe default only.",
                ),
                status(
                    "Sensitive data redaction",
                    "Configured",
                    "Logging policy forbids credentials, tokens, secrets, amounts, notes, search text, and encrypted payloads.",
                    DebugHealthSeverity.Ready,
                ),
            ),
        )

    private fun databaseSection(): DebugHealthSection =
        DebugHealthSection(
            title = "Database",
            statuses = listOf(
                status(
                    "Database",
                    PocketLedgerDatabase.DATABASE_NAME,
                    "Local Room database name.",
                ),
                status(
                    "Schema version",
                    DatabaseMigrations.CURRENT_VERSION.toString(),
                    "Current Room schema version.",
                    DebugHealthSeverity.Ready,
                ),
                status(
                    "Migration coverage",
                    "${DatabaseMigrations.ALL.size} registered",
                    "Registered migration count in DatabaseMigrations.ALL.",
                    DebugHealthSeverity.Ready,
                ),
            ),
        )

    private fun featureFlagSection(): DebugHealthSection =
        DebugHealthSection(
            title = "Feature Flags",
            statuses = featureFlagStates.ifEmpty {
                listOf(
                    status(
                        "Feature flags",
                        "Not configured",
                        "No safe feature flag summary is available.",
                    ),
                )
            },
        )

    private fun releaseSafetySection(): DebugHealthSection =
        DebugHealthSection(
            title = "Release Safety",
            statuses = listOf(
                status(
                    "Debug health route",
                    "Debug builds only",
                    "The debug destination is registered only when debug destinations are enabled.",
                    DebugHealthSeverity.Ready,
                ),
                status(
                    "Sensitive diagnostics",
                    "Hidden",
                    "Secrets, service accounts, keystore data, tester emails, tokens, and private CI metadata are never displayed.",
                    DebugHealthSeverity.Ready,
                ),
                status(
                    "Release diagnostics privacy",
                    "Release hidden",
                    "Release and benchmark variants do not register the debug health destination.",
                    DebugHealthSeverity.Ready,
                ),
            ),
        )

    private fun status(
        label: String,
        value: String,
        description: String,
        severity: DebugHealthSeverity = DebugHealthSeverity.Neutral,
    ): DebugHealthStatus =
        DebugHealthStatus(
            label = label,
            value = value,
            description = description,
            severity = severity,
        )

    companion object {
        fun from(
            context: Context,
            featureFlags: FeatureFlagEvaluator,
        ): DebugHealthReportFactory =
            DebugHealthReportFactory(
                buildInfo = DebugHealthBuildInfo(
                    appName = context.applicationInfo.loadLabel(context.packageManager).toString(),
                    versionName = BuildConfig.VERSION_NAME,
                    versionCode = BuildConfig.VERSION_CODE,
                    applicationId = BuildConfig.APPLICATION_ID,
                    buildType = BuildConfig.BUILD_TYPE,
                    flavor = "",
                    appEnvironment = BuildConfig.APP_ENV,
                    debuggable = context.applicationInfo.isDebuggable(),
                    internalBuild = BuildConfig.IS_INTERNAL_BUILD,
                    loggingEnabled = BuildConfig.LOGGING_ENABLED,
                    ci = System.getenv("CI").equals("true", ignoreCase = true),
                    firebaseConfigured = context.resources.getIdentifier(
                        "google_app_id",
                        "string",
                        context.packageName,
                    ) != 0,
                ),
                featureFlagStates = DefaultFeatureFlags.All
                    .filterIsInstance<BooleanFeatureFlag>()
                    .map { flag ->
                        val enabled = featureFlags.isEnabled(flag)
                        DebugHealthStatus(
                            label = flag.key.value,
                            value = enabledValue(enabled),
                            description = flag.description,
                            severity = DebugHealthSeverity.Neutral,
                        )
                    },
            )

        private fun ApplicationInfo.isDebuggable(): Boolean =
            (flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 || BuildConfig.DEBUG

        private fun enabledValue(enabled: Boolean): String =
            if (enabled) "Enabled" else "Disabled"
    }
}
