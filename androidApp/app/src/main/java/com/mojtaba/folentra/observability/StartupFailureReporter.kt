package com.mojtaba.folentra.observability

import com.mojtaba.folentra.core.security.logging.AppLogger
import com.mojtaba.folentra.core.security.logging.SensitiveValueRedactor
import java.time.Clock

interface StartupFailureReporter {
    val status: StartupFailureStatus

    fun markStartupStarted(stage: String)

    fun markStartupCompleted()

    fun recordCriticalFailure(
        throwable: Throwable,
        stage: String,
    )
}

data class StartupFailureStatus(
    val trackerEnabled: Boolean,
    val lastFailure: StartupFailureSnapshot?,
)

data class StartupFailureSnapshot(
    val stage: String,
    val throwableClassName: String,
    val occurredAtMillis: Long,
    val reportedToCrashReporter: Boolean,
)

class DefaultStartupFailureReporter(
    private val crashReporter: CrashReporter,
    private val appLogger: AppLogger,
    private val clock: Clock = Clock.systemUTC(),
    private val redactor: SensitiveValueRedactor = SensitiveValueRedactor(),
) : StartupFailureReporter {
    @Volatile
    private var snapshot: StartupFailureSnapshot? = null

    override val status: StartupFailureStatus
        get() = StartupFailureStatus(
            trackerEnabled = true,
            lastFailure = snapshot,
        )

    override fun markStartupStarted(stage: String) {
        appLogger.info("App startup started stage=${stage.safeValue()}")
    }

    override fun markStartupCompleted() {
        appLogger.info("App startup completed")
    }

    override fun recordCriticalFailure(
        throwable: Throwable,
        stage: String,
    ) {
        val safeStage = stage.safeValue()
        val reported = crashReporter.status.active
        snapshot = StartupFailureSnapshot(
            stage = safeStage,
            throwableClassName = throwable::class.java.name,
            occurredAtMillis = clock.millis(),
            reportedToCrashReporter = reported,
        )
        appLogger.error(
            throwable = throwable,
            message = "Critical startup failure stage=$safeStage",
        )
        crashReporter.recordException(
            throwable = throwable,
            event = CrashReportEvent(
                name = "critical_startup_failure",
                attributes = mapOf(
                    "stage" to safeStage,
                    "throwable_class" to throwable::class.java.name,
                ),
            ),
        )
    }

    private fun String.safeValue(): String =
        redactor.redact(this).take(MAX_ATTRIBUTE_LENGTH).ifBlank { "unknown" }

    private companion object {
        const val MAX_ATTRIBUTE_LENGTH = 80
    }
}

class NoOpStartupFailureReporter : StartupFailureReporter {
    override val status: StartupFailureStatus = StartupFailureStatus(
        trackerEnabled = false,
        lastFailure = null,
    )

    override fun markStartupStarted(stage: String) = Unit

    override fun markStartupCompleted() = Unit

    override fun recordCriticalFailure(
        throwable: Throwable,
        stage: String,
    ) = Unit
}
