package com.mojtaba.pocketledger.core.security.logging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeAppLoggerTest {
    @Test
    fun debugLogsAreSanitizedWhenDebugPolicyAllowsThem() {
        val sink = RecordingLogSink()
        val logger = SafeAppLogger(
            policy = LoggingPolicy.Debug,
            sink = sink,
        )

        logger.debug("Transaction loaded merchant=Starbucks note=Coffee")

        assertEquals(1, sink.entries.size)
        assertEquals(LogLevel.Debug, sink.entries.single().level)
        assertEquals(
            "Transaction loaded merchant=[REDACTED] note=[REDACTED]",
            sink.entries.single().message,
        )
    }

    @Test
    fun releasePolicySuppressesDebugLogs() {
        val sink = RecordingLogSink()
        val logger = SafeAppLogger(
            policy = LoggingPolicy.Release,
            sink = sink,
        )

        logger.debug("Transaction loaded amount=1200")

        assertTrue(sink.entries.isEmpty())
    }

    @Test
    fun releasePolicyAllowsSanitizedWarnings() {
        val sink = RecordingLogSink()
        val logger = SafeAppLogger(
            policy = LoggingPolicy.Release,
            sink = sink,
        )

        logger.warning("Cache refresh failed account_id=user-123")

        assertEquals(1, sink.entries.size)
        assertEquals(LogLevel.Warning, sink.entries.single().level)
        assertEquals(
            "Cache refresh failed account_id=[REDACTED]",
            sink.entries.single().message,
        )
    }

    @Test
    fun errorMessagesAndThrowablesAreSanitized() {
        val sink = RecordingLogSink()
        val logger = SafeAppLogger(
            policy = LoggingPolicy.Release,
            sink = sink,
        )

        logger.error(
            throwable = IllegalStateException("token=abcd note=Vacation"),
            message = "Transaction save failed merchant=Starbucks",
        )

        val entry = sink.entries.single()
        assertEquals(LogLevel.Error, entry.level)
        assertEquals("Transaction save failed merchant=[REDACTED]", entry.message)
        assertEquals(
            "java.lang.IllegalStateException: token=[REDACTED] note=[REDACTED]",
            entry.throwable.toString(),
        )
    }

    @Test
    fun nullableThrowableIsSupported() {
        val sink = RecordingLogSink()
        val logger = SafeAppLogger(
            policy = LoggingPolicy.Release,
            sink = sink,
        )

        logger.error(message = "Migration failed encrypted_payload=abc")

        val entry = sink.entries.single()
        assertEquals("Migration failed encrypted_payload=[REDACTED]", entry.message)
        assertNull(entry.throwable)
    }

    private class RecordingLogSink : LogSink {
        val entries = mutableListOf<Entry>()

        override fun log(
            level: LogLevel,
            tag: String,
            message: String,
            throwable: Throwable?,
        ) {
            entries += Entry(level, tag, message, throwable)
        }
    }

    private data class Entry(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?,
    )
}
