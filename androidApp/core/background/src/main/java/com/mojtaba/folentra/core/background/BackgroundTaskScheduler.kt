package com.mojtaba.folentra.core.background

interface BackgroundTaskScheduler {
    suspend fun enqueue(task: ScheduledTask): SchedulerResult

    suspend fun cancel(taskId: BackgroundTaskId): SchedulerResult

    suspend fun cancelUniqueWork(uniqueName: String): SchedulerResult

    suspend fun status(taskId: BackgroundTaskId): TaskStatus
}
