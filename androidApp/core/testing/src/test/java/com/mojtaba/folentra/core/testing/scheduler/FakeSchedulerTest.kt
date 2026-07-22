package com.mojtaba.folentra.core.testing.scheduler

import com.mojtaba.folentra.core.background.BackgroundTaskId
import com.mojtaba.folentra.core.background.ScheduledTask
import com.mojtaba.folentra.core.background.SchedulerResult
import com.mojtaba.folentra.core.background.TaskSchedule
import com.mojtaba.folentra.core.background.TaskStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FakeSchedulerTest {
    @Test
    fun enqueueRecordsTaskAndStatus() = runTest {
        val scheduler = FakeScheduler()
        val task = ScheduledTask(
            id = BackgroundTaskId("sync"),
            schedule = TaskSchedule.OneTime(),
        )

        assertEquals(SchedulerResult.Success, scheduler.enqueue(task))

        assertEquals(listOf(task), scheduler.enqueuedTasks)
        assertEquals(task, scheduler.scheduledTask(task.id))
        assertEquals(TaskStatus.Enqueued, scheduler.status(task.id))
    }

    @Test
    fun cancelRecordsTaskIdAndClearsTask() = runTest {
        val scheduler = FakeScheduler()
        val task = ScheduledTask(
            id = BackgroundTaskId("cleanup"),
            schedule = TaskSchedule.OneTime(),
        )
        scheduler.enqueue(task)

        assertEquals(SchedulerResult.Success, scheduler.cancel(task.id))

        assertEquals(listOf(task.id), scheduler.cancelledTaskIds)
        assertNull(scheduler.scheduledTask(task.id))
        assertEquals(TaskStatus.Cancelled, scheduler.status(task.id))
    }

    @Test
    fun configuredFailuresDoNotRecordSuccessfulState() = runTest {
        val scheduler = FakeScheduler()
        val task = ScheduledTask(
            id = BackgroundTaskId("budget-refresh"),
            schedule = TaskSchedule.OneTime(),
        )
        val failure = SchedulerResult.Failure("No worker")
        scheduler.enqueueResult = failure

        assertEquals(failure, scheduler.enqueue(task))

        assertNull(scheduler.scheduledTask(task.id))
        assertEquals(TaskStatus.NotScheduled, scheduler.status(task.id))
    }
}
