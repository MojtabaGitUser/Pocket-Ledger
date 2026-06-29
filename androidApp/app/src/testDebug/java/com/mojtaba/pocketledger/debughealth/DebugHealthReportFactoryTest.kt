package com.mojtaba.pocketledger.debughealth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugHealthReportFactoryTest {
    @Test
    fun reportIncludesExpectedSafeSectionsAndStatuses() {
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
                "Release Safety",
            ),
            report.sections.map { it.title },
        )
        assertStatus(report, "Build type", "debug")
        assertStatus(report, "PR validation", "Configured via CI")
        assertStatus(report, "App Distribution", "Configured via CI")
        assertStatus(report, "Release diagnostics privacy", "Release hidden")
    }

    @Test
    fun reportDoesNotExposeSensitiveDiagnosticFields() {
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

    private fun factory(): DebugHealthReportFactory =
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
                ci = false,
                firebaseConfigured = true,
            ),
            featureFlagStates = listOf(
                DebugHealthStatus(
                    label = "semantic_search_enabled",
                    value = "Disabled",
                    description = "Safe flag summary.",
                ),
            ),
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
