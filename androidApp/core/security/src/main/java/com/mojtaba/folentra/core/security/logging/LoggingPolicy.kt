package com.mojtaba.folentra.core.security.logging

data class LoggingPolicy(
    val allowedLevels: Set<LogLevel>,
) {
    fun allows(level: LogLevel): Boolean = level in allowedLevels

    companion object {
        val Debug = LoggingPolicy(
            allowedLevels = setOf(
                LogLevel.Debug,
                LogLevel.Info,
                LogLevel.Warning,
                LogLevel.Error,
            ),
        )

        val Release = LoggingPolicy(
            allowedLevels = setOf(
                LogLevel.Warning,
                LogLevel.Error,
            ),
        )

        val AllowedOperationalEvents: Set<String> = setOf(
            "app startup",
            "screen navigation",
            "repository lifecycle events",
            "sync lifecycle events",
            "cache events",
            "migration status",
            "WorkManager scheduling",
        )

        val ForbiddenData: Set<String> = setOf(
            "transaction note contents",
            "transaction amounts",
            "search text",
            "budget values",
            "credentials",
            "tokens",
            "secrets",
            "encrypted payloads",
        )
    }
}
