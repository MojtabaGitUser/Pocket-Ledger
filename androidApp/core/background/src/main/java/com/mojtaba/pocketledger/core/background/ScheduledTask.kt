package com.mojtaba.pocketledger.core.background

data class ScheduledTask(
    val id: BackgroundTaskId,
    val schedule: TaskSchedule,
    val constraints: TaskConstraints = TaskConstraints(),
    val policy: TaskPolicy = TaskPolicy.KeepExisting,
    val inputData: Map<String, Any> = emptyMap(),
) {
    val uniqueName: String = id.value
}