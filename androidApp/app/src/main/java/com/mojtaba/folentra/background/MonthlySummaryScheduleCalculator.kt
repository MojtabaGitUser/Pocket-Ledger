package com.mojtaba.folentra.background

import com.mojtaba.folentra.core.background.tasks.MonthlySummaryPreparationInput
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class MonthlySummaryScheduleCalculator(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun nextInput(time: MonthlySummaryReminderTime): ScheduledMonthlySummaryInput {
        val now = Instant.now(clock).atZone(zoneId)
        val todayAtTime = now.toLocalDate().atTime(time.hour, time.minute).atZone(zoneId)
        val runAt = if (todayAtTime.toInstant().isAfter(now.toInstant())) {
            todayAtTime
        } else {
            now.toLocalDate().plusDays(1).atTime(time.hour, time.minute).atZone(zoneId)
        }
        val previousMonth = YearMonth.from(runAt).minusMonths(1)
        val start = previousMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = previousMonth.atEndOfMonth().plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L
        val generatedAt = now.toInstant().toEpochMilli()
        val delayMillis = (runAt.toInstant().toEpochMilli() - generatedAt).coerceAtLeast(0L)
        return ScheduledMonthlySummaryInput(
            input = MonthlySummaryPreparationInput(
                periodStartMillis = start,
                periodEndMillis = end,
                periodLabel = previousMonth.atDay(1).format(PeriodFormatter),
                currencyCode = "USD",
                generatedAtMillis = generatedAt,
            ),
            initialDelay = delayMillis.milliseconds,
        )
    }

    private companion object {
        val PeriodFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    }
}

data class ScheduledMonthlySummaryInput(
    val input: MonthlySummaryPreparationInput,
    val initialDelay: Duration,
)

data class MonthlySummaryReminderTime(
    val hour: Int,
    val minute: Int,
) {
    init {
        require(hour in 0..23) { "Reminder hour must be between 0 and 23." }
        require(minute in 0..59) { "Reminder minute must be between 0 and 59." }
    }

    fun displayLabel(): String = LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("HH:mm"))

    companion object {
        val Default = MonthlySummaryReminderTime(hour = 9, minute = 0)
    }
}
