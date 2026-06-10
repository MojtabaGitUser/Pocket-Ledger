package com.mojtaba.pocketledger.core.background

import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ScheduledTaskTest {
    @Test
    fun backgroundTaskIdRejectsBlankValue() {
        assertThrows(IllegalArgumentException::class.java) {
            BackgroundTaskId(" ")
        }
    }

    @Test
    fun oneTimeScheduleRejectsNegativeDelay() {
        assertThrows(IllegalArgumentException::class.java) {
            TaskSchedule.OneTime(initialDelay = (-1).minutes)
        }
    }

    @Test
    fun periodicScheduleRejectsTooShortInterval() {
        assertThrows(IllegalArgumentException::class.java) {
            TaskSchedule.Periodic(repeatInterval = 14.minutes)
        }
    }

    @Test
    fun periodicScheduleRejectsTooShortFlexInterval() {
        assertThrows(IllegalArgumentException::class.java) {
            TaskSchedule.Periodic(
                repeatInterval = 30.minutes,
                flexInterval = 4.minutes,
            )
        }
    }

    @Test
    fun scheduledTaskUsesTaskIdAsUniqueName() {
        val task = ScheduledTask(
            id = BackgroundTaskId("sync"),
            schedule = TaskSchedule.OneTime(),
        )

        assertEquals("sync", task.uniqueName)
    }
}
