package com.mojtaba.pocketledger.observability

import com.mojtaba.pocketledger.core.security.logging.AppLogger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class StartupFailureReporterTest {
    @Test
    fun recordsCriticalStartupFailureAndSendsSanitizedCrashEvent() {
        val crashReporter = RecordingCrashReporter(
            status = CrashReportingStatus(
                provider = "test",
                configured = true,
                collectionEnabled = true,
            ),
        )
        val logger = RecordingLogger()
        val reporter = DefaultStartupFailureReporter(
            crashReporter = crashReporter,
            appLogger = logger,
            clock = Clock.fixed(Instant.ofEpochMilli(42L), ZoneOffset.UTC),
        )

        reporter.recordCriticalFailure(
            throwable = IllegalStateException("token=abcd note=Vacation"),
            stage = "app_graph_create token=secret",
        )

        val snapshot = requireNotNull(reporter.status.lastFailure)
        assertEquals("app_graph_create token=[REDACTED]", snapshot.stage)
        assertEquals(IllegalStateException::class.java.name, snapshot.throwableClassName)
        assertEquals(42L, snapshot.occurredAtMillis)
        assertTrue(snapshot.reportedToCrashReporter)
        assertEquals("critical_startup_failure", crashReporter.events.single().name)
        assertEquals("app_graph_create token=[REDACTED]", crashReporter.events.single().attributes.getValue("stage"))
        assertFalse(crashReporter.events.single().attributes.values.joinToString().contains("secret"))
        assertTrue(logger.errorMessages.single().contains("Critical startup failure"))
    }

    @Test
    fun startupLifecycleLogsSafeOperationalEvents() {
        val crashReporter = RecordingCrashReporter(
            status = CrashReportingStatus(provider = "test", configured = true, collectionEnabled = false),
        )
        val logger = RecordingLogger()
        val reporter = DefaultStartupFailureReporter(
            crashReporter = crashReporter,
            appLogger = logger,
        )

        reporter.markStartupStarted("main_activity_on_create")
        reporter.markStartupCompleted()

        assertEquals(
            listOf(
                "App startup started stage=main_activity_on_create",
                "App startup completed",
            ),
            logger.infoMessages,
        )
        assertTrue(crashReporter.events.isEmpty())
    }

    private class RecordingCrashReporter(
        override val status: CrashReportingStatus,
    ) : CrashReporter {
        val events = mutableListOf<CrashReportEvent>()

        override fun recordException(
            throwable: Throwable,
            event: CrashReportEvent,
        ) {
            if (status.active) events += event
        }
    }

    private class RecordingLogger : AppLogger {
        val infoMessages = mutableListOf<String>()
        val errorMessages = mutableListOf<String>()

        override fun debug(message: String) = Unit

        override fun info(message: String) {
            infoMessages += message
        }

        override fun warning(message: String) = Unit

        override fun error(
            throwable: Throwable?,
            message: String,
        ) {
            errorMessages += message
        }
    }
}
