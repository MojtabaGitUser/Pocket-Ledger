package com.mojtaba.pocketledger.core.background.tasks

import com.mojtaba.pocketledger.core.background.TaskPolicy
import com.mojtaba.pocketledger.core.background.TaskSchedule
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthlySummaryPreparationTaskTest {
    @Test
    fun scheduledTaskCarriesMonthlySummaryPayload() {
        val input = MonthlySummaryPreparationInput(
            periodStartMillis = 100L,
            periodEndMillis = 200L,
            periodLabel = "June 2026",
            currencyCode = "USD",
            generatedAtMillis = 300L,
        )

        val task = MonthlySummaryPreparationTask.scheduledTask(
            input = input,
            initialDelay = 15.minutes,
            policy = TaskPolicy.KeepExisting,
        )

        assertEquals(MonthlySummaryPreparationTask.Id, task.id)
        assertEquals(TaskPolicy.KeepExisting, task.policy)
        assertTrue(task.constraints.requiresBatteryNotLow)
        assertEquals(TaskSchedule.OneTime(initialDelay = 15.minutes), task.schedule)
        assertEquals(100L, task.inputData[MonthlySummaryPreparationTask.InputPeriodStartMillis])
        assertEquals(200L, task.inputData[MonthlySummaryPreparationTask.InputPeriodEndMillis])
        assertEquals("June 2026", task.inputData[MonthlySummaryPreparationTask.InputPeriodLabel])
        assertEquals("USD", task.inputData[MonthlySummaryPreparationTask.InputCurrencyCode])
        assertEquals(300L, task.inputData[MonthlySummaryPreparationTask.InputGeneratedAtMillis])
    }

    @Test
    fun monthlySummaryInputNormalizesDisplayValues() {
        val input = MonthlySummaryPreparationInput(
            periodStartMillis = 1L,
            periodEndMillis = 2L,
            periodLabel = "  June   2026  ",
            currencyCode = " usd ",
            generatedAtMillis = 3L,
        )

        assertEquals("June 2026", input.normalized().periodLabel)
        assertEquals("USD", input.normalized().currencyCode)
    }

    @Test
    fun monthlySummaryInputRejectsInvalidRangesAndBlankFields() {
        assertThrows(IllegalArgumentException::class.java) {
            MonthlySummaryPreparationInput(
                periodStartMillis = 2L,
                periodEndMillis = 1L,
                periodLabel = "June 2026",
                currencyCode = "USD",
                generatedAtMillis = 3L,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            MonthlySummaryPreparationInput(
                periodStartMillis = 1L,
                periodEndMillis = 2L,
                periodLabel = " ",
                currencyCode = "USD",
                generatedAtMillis = 3L,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            MonthlySummaryPreparationInput(
                periodStartMillis = 1L,
                periodEndMillis = 2L,
                periodLabel = "June 2026",
                currencyCode = " ",
                generatedAtMillis = 3L,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            MonthlySummaryPreparationInput(
                periodStartMillis = 1L,
                periodEndMillis = 2L,
                periodLabel = "June 2026",
                currencyCode = "USD",
                generatedAtMillis = -1L,
            )
        }
    }
}
