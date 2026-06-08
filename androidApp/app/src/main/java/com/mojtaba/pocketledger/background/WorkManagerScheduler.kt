package com.mojtaba.pocketledger.background

import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.mojtaba.pocketledger.core.background.BackgroundTaskId
import com.mojtaba.pocketledger.core.background.BackgroundTaskScheduler
import com.mojtaba.pocketledger.core.background.ScheduledTask
import com.mojtaba.pocketledger.core.background.SchedulerResult
import com.mojtaba.pocketledger.core.background.TaskSchedule
import com.mojtaba.pocketledger.core.background.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkManagerScheduler(
    private val workManager: WorkManager,
    private val workerRegistry: TaskWorkerRegistry,
) : BackgroundTaskScheduler {
    override suspend fun enqueue(task: ScheduledTask): SchedulerResult =
        runCatching {
            val workerClass = workerRegistry.workerClassFor(task.id)
                ?: return SchedulerResult.Failure("No worker is registered for task: ${task.id}")
            val request = WorkManagerTaskMapper.workRequest(task, workerClass)
            when (task.schedule) {
                is TaskSchedule.OneTime -> workManager.enqueueUniqueWork(
                    task.uniqueName,
                    WorkManagerTaskMapper.oneTimePolicy(task.policy),
                    request as OneTimeWorkRequest,
                )
                is TaskSchedule.Periodic -> workManager.enqueueUniquePeriodicWork(
                    task.uniqueName,
                    WorkManagerTaskMapper.periodicPolicy(task.policy),
                    request as PeriodicWorkRequest,
                )
            }
        }.fold(
            onSuccess = { SchedulerResult.Success },
            onFailure = { throwable ->
                SchedulerResult.Failure(
                    message = throwable.message ?: "Unable to enqueue background task.",
                    cause = throwable,
                )
            },
        )

    override suspend fun cancel(taskId: BackgroundTaskId): SchedulerResult =
        cancelUniqueWork(taskId.value)

    override suspend fun cancelUniqueWork(uniqueName: String): SchedulerResult =
        runCatching {
            workManager.cancelUniqueWork(uniqueName)
        }.fold(
            onSuccess = { SchedulerResult.Success },
            onFailure = { throwable ->
                SchedulerResult.Failure(
                    message = throwable.message ?: "Unable to cancel background task.",
                    cause = throwable,
                )
            },
        )

    override suspend fun status(taskId: BackgroundTaskId): TaskStatus =
        withContext(Dispatchers.IO) {
            runCatching {
                workManager.getWorkInfosForUniqueWork(taskId.value)
                    .get()
                    .firstOrNull()
                    ?.state
            }.fold(
                onSuccess = WorkManagerTaskMapper::workStatus,
                onFailure = { TaskStatus.Unknown },
            )
        }
}
