package com.mojtaba.folentra.core.background

class BackgroundTaskRegistry(
    tasks: Set<RegisteredBackgroundTask>,
) {
    private val tasksById: Map<BackgroundTaskId, RegisteredBackgroundTask> =
        tasks.associateBy { task -> task.id }

    fun requireTask(taskId: BackgroundTaskId): RegisteredBackgroundTask =
        tasksById[taskId] ?: error("Background task is not registered: $taskId")

    fun contains(taskId: BackgroundTaskId): Boolean = taskId in tasksById

    fun registeredTasks(): Set<RegisteredBackgroundTask> = tasksById.values.toSet()
}
