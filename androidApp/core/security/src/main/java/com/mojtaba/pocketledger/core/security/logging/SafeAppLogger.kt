package com.mojtaba.pocketledger.core.security.logging

class SafeAppLogger(
    private val policy: LoggingPolicy,
    private val redactor: SensitiveValueRedactor = SensitiveValueRedactor(),
    private val sink: LogSink = AndroidLogSink,
    private val tag: String = DEFAULT_TAG,
) : AppLogger {
    override fun debug(message: String) {
        log(LogLevel.Debug, message, throwable = null)
    }

    override fun info(message: String) {
        log(LogLevel.Info, message, throwable = null)
    }

    override fun warning(message: String) {
        log(LogLevel.Warning, message, throwable = null)
    }

    override fun error(
        throwable: Throwable?,
        message: String,
    ) {
        log(LogLevel.Error, message, throwable)
    }

    private fun log(
        level: LogLevel,
        message: String,
        throwable: Throwable?,
    ) {
        if (!policy.allows(level)) return

        sink.log(
            level = level,
            tag = tag,
            message = redactor.redact(message),
            throwable = throwable?.sanitized(),
        )
    }

    private fun Throwable.sanitized(): Throwable =
        SanitizedLoggedThrowable(
            originalClassName = this::class.java.name,
            sanitizedMessage = message?.let(redactor::redact),
        )

    private class SanitizedLoggedThrowable(
        private val originalClassName: String,
        private val sanitizedMessage: String?,
    ) : RuntimeException(sanitizedMessage) {
        override fun toString(): String =
            if (sanitizedMessage.isNullOrBlank()) {
                originalClassName
            } else {
                "$originalClassName: $sanitizedMessage"
            }

        override fun fillInStackTrace(): Throwable = this
    }

    private companion object {
        const val DEFAULT_TAG = "PocketLedger"
    }
}
