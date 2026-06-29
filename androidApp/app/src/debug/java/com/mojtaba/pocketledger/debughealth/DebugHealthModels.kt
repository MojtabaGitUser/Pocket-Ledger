package com.mojtaba.pocketledger.debughealth

data class DebugHealthReport(
    val sections: List<DebugHealthSection>,
) {
    val allStatuses: List<DebugHealthStatus> = sections.flatMap { it.statuses }
}

data class DebugHealthSection(
    val title: String,
    val statuses: List<DebugHealthStatus>,
)

data class DebugHealthStatus(
    val label: String,
    val value: String,
    val description: String,
    val severity: DebugHealthSeverity = DebugHealthSeverity.Neutral,
) {
    val accessibilityState: String = "$label: $value"
}

enum class DebugHealthSeverity {
    Ready,
    Neutral,
    Warning,
}

data class DebugHealthBuildInfo(
    val appName: String,
    val versionName: String,
    val versionCode: Int,
    val applicationId: String,
    val buildType: String,
    val flavor: String,
    val appEnvironment: String,
    val debuggable: Boolean,
    val internalBuild: Boolean,
    val loggingEnabled: Boolean,
    val ci: Boolean,
    val firebaseConfigured: Boolean,
)
