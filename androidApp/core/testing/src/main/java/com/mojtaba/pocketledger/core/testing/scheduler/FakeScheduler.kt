package com.mojtaba.pocketledger.core.testing.scheduler

import com.mojtaba.pocketledger.core.background.BackgroundTaskId
import com.mojtaba.pocketledger.core.background.BackgroundTaskScheduler
import com.mojtaba.pocketledger.core.background.ScheduledTask
import com.mojtaba.pocketledger.core.background.SchedulerResult
import com.mojtaba.pocketledger.core.background.TaskStatus

class FakeScheduler : BackgroundTaskScheduler {
    private val scheduledTasks = mutableMapOf<BackgroundTaskId, ScheduledTask>()
    private val configuredStatuses = mutableMapOf<BackgroundTaskId, TaskStatus>()

    val enqueuedTasks: List<ScheduledTask>
        get() = scheduledTasks.values.toList()

    val cancelledTaskIds = mutableListOf<BackgroundTaskId>()
    val cancelledUniqueNames = mutableListOf<String>()

    var enqueueResult: SchedulerResult = SchedulerResult.Success
    var cancelResult: SchedulerResult = SchedulerResult.Success

    override suspend fun enqueue(task: ScheduledTask): SchedulerResult {
        if (enqueueResult is SchedulerResult.Success) {
            scheduledTasks[task.id] = task
            configuredStatuses[task.id] = TaskStatus.Enqueued
        }
        return enqueueResult
    }

    override suspend fun cancel(taskId: BackgroundTaskId): SchedulerResult {
        cancelledTaskIds += taskId
        if (cancelResult is SchedulerResult.Success) {
            scheduledTasks -= taskId
            configuredStatuses[taskId] = TaskStatus.Cancelled
        }
        return cancelResult
    }

    override suspend fun cancelUniqueWork(uniqueName: String): SchedulerResult {
        cancelledUniqueNames += uniqueName
        if (cancelResult is SchedulerResult.Success) {
            scheduledTasks.entries
                .filter { (_, task) -> task.uniqueName == uniqueName }
                .forEach { (taskId, _) ->
                    scheduledTasks -= taskId
                    configuredStatuses[taskId] = TaskStatus.Cancelled
                }
        }
        return cancelResult
    }

    override suspend fun status(taskId: BackgroundTaskId): TaskStatus =
        configuredStatuses[taskId] ?: TaskStatus.NotScheduled

    fun setStatus(
        taskId: BackgroundTaskId,
        status: TaskStatus,
    ) {
        configuredStatuses[taskId] = status
    }

    fun scheduledTask(taskId: BackgroundTaskId): ScheduledTask? = scheduledTasks[taskId]

    fun clear() {
        scheduledTasks.clear()
        configuredStatuses.clear()
        cancelledTaskIds.clear()
        cancelledUniqueNames.clear()
        enqueueResult = SchedulerResult.Success
        cancelResult = SchedulerResult.Success
    }
}
