package com.mojtaba.folentra.core.background.tasks

import com.mojtaba.folentra.core.background.BackgroundTaskId
import com.mojtaba.folentra.core.background.RegisteredBackgroundTask
import com.mojtaba.folentra.core.background.ScheduledTask
import com.mojtaba.folentra.core.background.TaskConstraints
import com.mojtaba.folentra.core.background.TaskPolicy
import com.mojtaba.folentra.core.background.TaskSchedule
import java.util.Locale

object MonthlySummaryPreparationTask {
    val Id = BackgroundTaskId("monthly-summary-preparation")

    const val InputPeriodStartMillis = "period_start_millis"
    const val InputPeriodEndMillis = "period_end_millis"
    const val InputPeriodLabel = "period_label"
    const val InputCurrencyCode = "currency_code"
    const val InputGeneratedAtMillis = "generated_at_millis"

    val Definition = RegisteredBackgroundTask(
        id = Id,
        description = "Prepare the local monthly dashboard summary",
    )

    fun scheduledTask(
        input: MonthlySummaryPreparationInput,
        initialDelay: kotlin.time.Duration,
        policy: TaskPolicy = TaskPolicy.ReplaceExisting,
    ): ScheduledTask = ScheduledTask(
        id = Id,
        schedule = TaskSchedule.OneTime(initialDelay = initialDelay),
        constraints = TaskConstraints(requiresBatteryNotLow = true),
        policy = policy,
        inputData = mapOf(
            InputPeriodStartMillis to input.periodStartMillis,
            InputPeriodEndMillis to input.periodEndMillis,
            InputPeriodLabel to input.periodLabel,
            InputCurrencyCode to input.currencyCode,
            InputGeneratedAtMillis to input.generatedAtMillis,
        ),
    )
}

data class MonthlySummaryPreparationInput(
    val periodStartMillis: Long,
    val periodEndMillis: Long,
    val periodLabel: String,
    val currencyCode: String,
    val generatedAtMillis: Long,
) {
    init {
        require(periodStartMillis <= periodEndMillis) { "Monthly summary period start must be before or equal to end." }
        require(periodLabel.isNotBlank()) { "Monthly summary period label must not be blank." }
        require(currencyCode.isNotBlank()) { "Monthly summary currency code must not be blank." }
        require(generatedAtMillis >= 0L) { "Monthly summary generatedAtMillis must not be negative." }
    }

    fun normalized(): MonthlySummaryPreparationInput = copy(
        periodLabel = periodLabel.trim().replace(Regex("\\s+"), " "),
        currencyCode = currencyCode.trim().uppercase(Locale.US),
    )
}
