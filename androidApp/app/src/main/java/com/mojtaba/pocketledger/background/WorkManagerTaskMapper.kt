package com.mojtaba.pocketledger.background

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkRequest
import com.mojtaba.pocketledger.core.background.ScheduledTask
import com.mojtaba.pocketledger.core.background.TaskConstraints
import com.mojtaba.pocketledger.core.background.TaskPolicy
import com.mojtaba.pocketledger.core.background.TaskSchedule
import com.mojtaba.pocketledger.core.background.TaskStatus
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object WorkManagerTaskMapper {
    fun oneTimePolicy(policy: TaskPolicy): ExistingWorkPolicy =
        when (policy) {
            TaskPolicy.KeepExisting -> ExistingWorkPolicy.KEEP
            TaskPolicy.ReplaceExisting -> ExistingWorkPolicy.REPLACE
            TaskPolicy.Append -> ExistingWorkPolicy.APPEND
        }

    fun periodicPolicy(policy: TaskPolicy): ExistingPeriodicWorkPolicy =
        when (policy) {
            TaskPolicy.KeepExisting -> ExistingPeriodicWorkPolicy.KEEP
            TaskPolicy.ReplaceExisting,
            TaskPolicy.Append,
            -> ExistingPeriodicWorkPolicy.UPDATE
        }

    fun constraints(constraints: TaskConstraints): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(
                if (constraints.requiresNetwork) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED,
            )
            .setRequiresCharging(constraints.requiresCharging)
            .setRequiresBatteryNotLow(constraints.requiresBatteryNotLow)
            .setRequiresDeviceIdle(constraints.requiresDeviceIdle)
            .build()

    fun workStatus(state: WorkInfo.State?): TaskStatus =
        when (state) {
            null -> TaskStatus.NotScheduled
            WorkInfo.State.ENQUEUED -> TaskStatus.Enqueued
            WorkInfo.State.RUNNING -> TaskStatus.Running
            WorkInfo.State.SUCCEEDED -> TaskStatus.Succeeded
            WorkInfo.State.FAILED -> TaskStatus.Failed
            WorkInfo.State.CANCELLED -> TaskStatus.Cancelled
            WorkInfo.State.BLOCKED -> TaskStatus.Blocked
        }

    fun workRequest(
        task: ScheduledTask,
        workerClass: Class<out ListenableWorker>,
    ): WorkRequest =
        when (val schedule = task.schedule) {
            is TaskSchedule.OneTime -> oneTimeWorkRequest(task, schedule, workerClass)
            is TaskSchedule.Periodic -> periodicWorkRequest(task, schedule, workerClass)
        }

    private fun oneTimeWorkRequest(
        task: ScheduledTask,
        schedule: TaskSchedule.OneTime,
        workerClass: Class<out ListenableWorker>,
    ): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(workerClass)
            .setConstraints(constraints(task.constraints))
            .setInitialDelay(schedule.initialDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, MinimumBackoff.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .addTag(task.id.value)
            .build()

    private fun periodicWorkRequest(
        task: ScheduledTask,
        schedule: TaskSchedule.Periodic,
        workerClass: Class<out ListenableWorker>,
    ): PeriodicWorkRequest {
        val flexInterval = schedule.flexInterval
        val builder = if (flexInterval == null) {
            PeriodicWorkRequest.Builder(
                workerClass,
                schedule.repeatInterval.inWholeMilliseconds,
                TimeUnit.MILLISECONDS,
            )
        } else {
            PeriodicWorkRequest.Builder(
                workerClass,
                schedule.repeatInterval.inWholeMilliseconds,
                TimeUnit.MILLISECONDS,
                flexInterval.inWholeMilliseconds,
                TimeUnit.MILLISECONDS,
            )
        }

        return builder
            .setConstraints(constraints(task.constraints))
            .setInitialDelay(schedule.initialDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, MinimumBackoff.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .addTag(task.id.value)
            .build()
    }

    private val MinimumBackoff: Duration = 10.minutes
}
