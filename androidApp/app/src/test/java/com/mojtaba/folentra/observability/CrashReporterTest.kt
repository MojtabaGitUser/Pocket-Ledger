package com.mojtaba.folentra.observability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashReporterTest {
    @Test
    fun releaseReporterSendsSanitizedNonFatalEvent() {
        val client = RecordingCrashlyticsClient()
        val reporter = FirebaseCrashReporter(
            crashlytics = client,
            status = CrashReportingStatus(
                provider = "Firebase Crashlytics",
                configured = true,
                collectionEnabled = true,
            ),
        )
        val original = IllegalStateException(
            "merchant=Coffee Shop note=Team lunch amount=42.50 token=secret",
        )
        original.stackTrace = arrayOf(StackTraceElement("Example", "start", "Example.kt", 12))

        reporter.recordException(
            throwable = original,
            event = CrashReportEvent(
                name = "non_fatal_validation_probe",
                attributes = mapOf(
                    "stage" to "startup",
                    "query" to "coffee near work",
                ),
            ),
        )

        assertTrue(client.collectionEnabledState)
        assertEquals("non_fatal_validation_probe", client.keys.getValue("event"))
        assertFalse(client.keys.containsKey("query"))
        val recorded = requireNotNull(client.exception)
        assertFalse(recorded.toString().contains("Coffee Shop"))
        assertFalse(recorded.toString().contains("Team lunch"))
        assertFalse(recorded.toString().contains("42.50"))
        assertFalse(recorded.toString().contains("secret"))
        assertEquals(original.stackTrace.toList(), recorded.stackTrace.toList())
    }

    @Test
    fun disabledReporterDoesNotSendAnything() {
        val client = RecordingCrashlyticsClient()
        val reporter = FirebaseCrashReporter(
            crashlytics = client,
            status = CrashReportingStatus(
                provider = "Firebase Crashlytics",
                configured = true,
                collectionEnabled = false,
            ),
        )

        reporter.recordException(
            IllegalStateException("note=private"),
            CrashReportEvent("disabled_probe"),
        )

        assertFalse(client.collectionEnabledState)
        assertTrue(client.keys.isEmpty())
        assertEquals(null, client.exception)
    }

    @Test
    fun keysAndValuesAreNormalizedAndBounded() {
        val client = RecordingCrashlyticsClient()
        val reporter = FirebaseCrashReporter(
            crashlytics = client,
            status = CrashReportingStatus("Firebase", configured = true, collectionEnabled = true),
        )

        reporter.recordException(
            RuntimeException("safe"),
            CrashReportEvent(
                name = "x".repeat(200),
                attributes = mapOf(
                    "operation" to "y".repeat(200),
                ),
            ),
        )

        assertEquals(setOf("event", "operation"), client.keys.keys)
        assertTrue(client.keys.values.all { it.length <= 100 })
    }

    private class RecordingCrashlyticsClient : CrashlyticsClient {
        var collectionEnabledState: Boolean = false
        val keys = linkedMapOf<String, String>()
        var exception: Throwable? = null

        override fun setCollectionEnabled(enabled: Boolean) {
            collectionEnabledState = enabled
        }

        override fun setCustomKey(key: String, value: String) {
            keys[key] = value
        }

        override fun recordException(throwable: Throwable) {
            exception = throwable
        }
    }
}
