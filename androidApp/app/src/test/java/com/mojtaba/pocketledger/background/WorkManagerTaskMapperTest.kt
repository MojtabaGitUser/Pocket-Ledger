package com.mojtaba.pocketledger.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mojtaba.pocketledger.core.background.BackgroundTaskId
import com.mojtaba.pocketledger.core.background.ScheduledTask
import com.mojtaba.pocketledger.core.background.TaskConstraints
import com.mojtaba.pocketledger.core.background.TaskPolicy
import com.mojtaba.pocketledger.core.background.TaskSchedule
import com.mojtaba.pocketledger.core.background.TaskStatus
import kotlin.time.Duration.Companion.hours
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkManagerTaskMapperTest {
    @Test
    fun mapsOneTimePolicy() {
        assertEquals(ExistingWorkPolicy.KEEP, WorkManagerTaskMapper.oneTimePolicy(TaskPolicy.KeepExisting))
        assertEquals(ExistingWorkPolicy.REPLACE, WorkManagerTaskMapper.oneTimePolicy(TaskPolicy.ReplaceExisting))
        assertEquals(ExistingWorkPolicy.APPEND, WorkManagerTaskMapper.oneTimePolicy(TaskPolicy.Append))
    }

    @Test
    fun mapsPeriodicPolicy() {
        assertEquals(ExistingPeriodicWorkPolicy.KEEP, WorkManagerTaskMapper.periodicPolicy(TaskPolicy.KeepExisting))
        assertEquals(ExistingPeriodicWorkPolicy.UPDATE, WorkManagerTaskMapper.periodicPolicy(TaskPolicy.ReplaceExisting))
        assertEquals(ExistingPeriodicWorkPolicy.UPDATE, WorkManagerTaskMapper.periodicPolicy(TaskPolicy.Append))
    }

    @Test
    fun mapsConstraints() {
        val constraints = WorkManagerTaskMapper.constraints(
            TaskConstraints(
                requiresNetwork = true,
                requiresCharging = true,
                requiresBatteryNotLow = true,
                requiresDeviceIdle = true,
            ),
        )

        assertEquals(NetworkType.CONNECTED, constraints.requiredNetworkType)
        assertTrue(constraints.requiresCharging())
        assertTrue(constraints.requiresBatteryNotLow())
        assertTrue(constraints.requiresDeviceIdle())
    }

    @Test
    fun createsOneTimeWorkRequestWithTaskTagAndConstraints() {
        val task = ScheduledTask(
            id = BackgroundTaskId("sync"),
            schedule = TaskSchedule.OneTime(),
            constraints = TaskConstraints(requiresNetwork = true),
        )

        val request = WorkManagerTaskMapper.workRequest(task, TestWorker::class.java)

        assertTrue(request is OneTimeWorkRequest)
        assertTrue(request.tags.contains("sync"))
        assertEquals(NetworkType.CONNECTED, request.workSpec.constraints.requiredNetworkType)
    }

    @Test
    fun createsPeriodicWorkRequestWithTaskTagAndConstraints() {
        val task = ScheduledTask(
            id = BackgroundTaskId("cleanup"),
            schedule = TaskSchedule.Periodic(repeatInterval = 1.hours),
            constraints = TaskConstraints(requiresCharging = true),
        )

        val request = WorkManagerTaskMapper.workRequest(task, TestWorker::class.java)

        assertTrue(request is PeriodicWorkRequest)
        assertTrue(request.tags.contains("cleanup"))
        assertEquals(Constraints.NONE.requiredNetworkType, request.workSpec.constraints.requiredNetworkType)
        assertTrue(request.workSpec.constraints.requiresCharging())
    }

    @Test
    fun mapsWorkInfoStateToTaskStatus() {
        assertEquals(TaskStatus.NotScheduled, WorkManagerTaskMapper.workStatus(null))
        assertEquals(TaskStatus.Enqueued, WorkManagerTaskMapper.workStatus(WorkInfo.State.ENQUEUED))
        assertEquals(TaskStatus.Running, WorkManagerTaskMapper.workStatus(WorkInfo.State.RUNNING))
        assertEquals(TaskStatus.Succeeded, WorkManagerTaskMapper.workStatus(WorkInfo.State.SUCCEEDED))
        assertEquals(TaskStatus.Failed, WorkManagerTaskMapper.workStatus(WorkInfo.State.FAILED))
        assertEquals(TaskStatus.Cancelled, WorkManagerTaskMapper.workStatus(WorkInfo.State.CANCELLED))
        assertEquals(TaskStatus.Blocked, WorkManagerTaskMapper.workStatus(WorkInfo.State.BLOCKED))
    }

    class TestWorker(
        appContext: Context,
        workerParams: WorkerParameters,
    ) : Worker(appContext, workerParams) {
        override fun doWork(): Result =
            Result.success()
    }
}
