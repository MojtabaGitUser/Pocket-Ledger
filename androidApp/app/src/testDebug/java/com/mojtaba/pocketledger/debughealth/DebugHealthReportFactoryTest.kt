package com.mojtaba.pocketledger.debughealth

import com.mojtaba.pocketledger.core.background.TaskStatus
import com.mojtaba.pocketledger.core.background.tasks.MonthlySummaryPreparationTask
import com.mojtaba.pocketledger.core.testing.scheduler.FakeScheduler
import com.mojtaba.pocketledger.observability.CrashReportingStatus
import com.mojtaba.pocketledger.observability.StartupFailureSnapshot
import com.mojtaba.pocketledger.observability.StartupFailureStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugHealthReportFactoryTest {
    @Test
    fun reportIncludesExpectedSafeSectionsAndStatuses() = runTest {
        val report = factory().create()

        assertEquals(
            listOf(
                "Build",
                "CI/CD",
                "Testing",
                "Firebase/App Distribution",
                "Observability",
                "Database",
                "Feature Flags",
                "Background Jobs",
                "Release Safety",
            ),
            report.sections.map { it.title },
        )
        assertStatus(report, "Build type", "debug")
        assertStatus(report, "PR validation", "Configured via CI")
        assertStatus(report, "App Distribution", "Configured via CI")
        assertStatus(report, "Crash reporting", "Configured but disabled")
        assertStatus(report, "Crash collection gate", "Disabled")
        assertStatus(report, "Startup failure tracker", "Enabled")
        assertStatus(report, "Last critical startup failure", "None recorded")
        assertStatus(report, "Product event taxonomy", "Configured")
        assertStatus(report, "Analytics provider", "Debug sink")
        assertStatus(report, "Monthly summary preparation", "Not scheduled")
        assertStatus(report, "Release diagnostics privacy", "Release hidden")
    }

    @Test
    fun reportIncludesActiveCrashReportingStatus() = runTest {
        val report = factory(
            crashReportingStatus = CrashReportingStatus(
                provider = "Firebase Crashlytics",
                configured = true,
                collectionEnabled = true,
            ),
            crashReportingEnabled = true,
        ).create()

        assertStatus(report, "Crash reporting", "Configured and enabled")
        assertStatus(report, "Crash collection gate", "Enabled")
    }

    @Test
    fun reportIncludesStartupFailureSnapshotWithoutRawThrowableMessage() = runTest {
        val report = factory(
            startupFailureStatus = StartupFailureStatus(
                trackerEnabled = true,
                lastFailure = StartupFailureSnapshot(
                    stage = "app_graph_create",
                    throwableClassName = IllegalStateException::class.java.name,
                    occurredAtMillis = 1_234L,
                    reportedToCrashReporter = true,
                ),
            ),
        ).create()

        assertStatus(report, "Last critical startup failure", "IllegalStateException at app_graph_create reported=true")
    }

    @Test
    fun reportIncludesBackgroundWorkerStatus() = runTest {
        val scheduler = FakeScheduler().apply {
            setStatus(MonthlySummaryPreparationTask.Id, TaskStatus.Running)
        }
        val report = factory(scheduler = scheduler, backgroundJobsEnabled = true).create()

        assertStatus(report, "Global background jobs", "Enabled")
        assertStatus(report, "Monthly summary preparation", "Running")
    }

    @Test
    fun reportDoesNotExposeSensitiveDiagnosticFields() = runTest {
        val renderedText = factory().create().allStatuses.joinToString(separator = "\n") {
            "${it.label} ${it.value} ${it.description}"
        }.lowercase()

        listOf(
            "FIREBASE_SERVICE_ACCOUNT_JSON".lowercase(),
            "FIREBASE_TESTER_GROUPS".lowercase(),
            "BEGIN PRIVATE KEY".lowercase(),
            "firebase_app_id=",
            "token value",
            "@example.com",
        ).forEach { forbidden ->
            assertFalse("Report exposed forbidden text: $forbidden", renderedText.contains(forbidden))
        }
    }

    @Test
    fun accessibilityStateCombinesLabelAndValue() {
        val status = DebugHealthStatus(
            label = "Logging mode",
            value = "Debug logging sanitized",
            description = "Safe logging status.",
        )

        assertEquals("Logging mode: Debug logging sanitized", status.accessibilityState)
    }

    private fun factory(
        scheduler: FakeScheduler = FakeScheduler(),
        backgroundJobsEnabled: Boolean = false,
        crashReportingStatus: CrashReportingStatus = CrashReportingStatus(
            provider = "Firebase Crashlytics",
            configured = true,
            collectionEnabled = false,
        ),
        crashReportingEnabled: Boolean = false,
        startupFailureStatus: StartupFailureStatus = StartupFailureStatus(
            trackerEnabled = true,
            lastFailure = null,
        ),
    ): DebugHealthReportFactory =
        DebugHealthReportFactory(
            buildInfo = DebugHealthBuildInfo(
                appName = "Pocket Ledger",
                versionName = "1.0-debug",
                versionCode = 1,
                applicationId = "com.mojtaba.pocketledger.debug",
                buildType = "debug",
                flavor = "",
                appEnvironment = "debug",
                debuggable = true,
                internalBuild = true,
                loggingEnabled = true,
                crashReportingEnabled = crashReportingEnabled,
                ci = false,
                firebaseConfigured = true,
                analyticsProviderState = "Debug sink",
            ),
            featureFlagStates = listOf(
                DebugHealthStatus(
                    label = "semantic_search_enabled",
                    value = "Disabled",
                    description = "Safe flag summary.",
                ),
            ),
            backgroundTaskScheduler = scheduler,
            backgroundJobsEnabled = backgroundJobsEnabled,
            crashReportingStatus = crashReportingStatus,
            startupFailureStatus = startupFailureStatus,
        )

    private fun assertStatus(
        report: DebugHealthReport,
        label: String,
        value: String,
    ) {
        assertTrue(
            "Missing status $label=$value",
            report.allStatuses.any { it.label == label && it.value == value },
        )
    }
}
