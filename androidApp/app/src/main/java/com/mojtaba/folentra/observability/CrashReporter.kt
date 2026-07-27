package com.mojtaba.folentra.observability

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mojtaba.folentra.core.security.logging.SensitiveValueRedactor

interface CrashReporter {
    val status: CrashReportingStatus

    fun recordException(
        throwable: Throwable,
        event: CrashReportEvent,
    )
}

data class CrashReportingStatus(
    val provider: String,
    val configured: Boolean,
    val collectionEnabled: Boolean,
) {
    val active: Boolean = configured && collectionEnabled
}

data class CrashReportEvent(
    val name: String,
    val attributes: Map<String, String> = emptyMap(),
)

class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics,
    override val status: CrashReportingStatus,
    private val redactor: SensitiveValueRedactor = SensitiveValueRedactor(),
) : CrashReporter {
    init {
        crashlytics.setCrashlyticsCollectionEnabled(status.collectionEnabled)
    }

    override fun recordException(
        throwable: Throwable,
        event: CrashReportEvent,
    ) {
        if (!status.active) return

        crashlytics.setCustomKey("event", event.name.safeAttributeValue())
        event.attributes.forEach { (key, value) ->
            crashlytics.setCustomKey(key.safeAttributeKey(), value.safeAttributeValue())
        }
        crashlytics.recordException(throwable.sanitized())
    }

    private fun Throwable.sanitized(): Throwable =
        SanitizedCrashThrowable(
            originalClassName = this::class.java.name,
            sanitizedMessage = message?.let(redactor::redact),
        )

    private fun String.safeAttributeKey(): String =
        replace(Regex("[^A-Za-z0-9_.-]"), "_").take(MAX_ATTRIBUTE_LENGTH).ifBlank { "attribute" }

    private fun String.safeAttributeValue(): String =
        redactor.redact(this).take(MAX_ATTRIBUTE_LENGTH)

    private class SanitizedCrashThrowable(
        private val originalClassName: String,
        private val sanitizedMessage: String?,
    ) : RuntimeException(sanitizedMessage) {
        override fun toString(): String =
            if (sanitizedMessage.isNullOrBlank()) {
                originalClassName
            } else {
                "$originalClassName: $sanitizedMessage"
            }
    }

    private companion object {
        const val MAX_ATTRIBUTE_LENGTH = 100
    }
}

class NoOpCrashReporter(
    override val status: CrashReportingStatus,
) : CrashReporter {
    override fun recordException(
        throwable: Throwable,
        event: CrashReportEvent,
    ) = Unit
}

object CrashReporterFactory {
    fun create(
        context: Context,
        collectionEnabled: Boolean,
    ): CrashReporter {
        val firebaseConfigured = context.resources.getIdentifier(
            "google_app_id",
            "string",
            context.packageName,
        ) != 0
        val status = CrashReportingStatus(
            provider = "Firebase Crashlytics",
            configured = firebaseConfigured,
            collectionEnabled = firebaseConfigured && collectionEnabled,
        )

        return if (firebaseConfigured) {
            FirebaseCrashReporter(
                crashlytics = FirebaseCrashlytics.getInstance(),
                status = status,
            )
        } else {
            NoOpCrashReporter(status = status)
        }
    }
}
