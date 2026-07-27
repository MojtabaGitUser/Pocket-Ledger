package com.mojtaba.folentra.background

import com.mojtaba.folentra.core.background.BackgroundTaskScheduler
import com.mojtaba.folentra.core.background.SchedulerResult
import com.mojtaba.folentra.core.background.tasks.MonthlySummaryPreparationTask
import com.mojtaba.folentra.core.featureflags.DefaultFeatureFlags
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.folentra.core.security.preferences.BooleanPreferenceKey
import com.mojtaba.folentra.core.security.preferences.LongPreferenceKey
import com.mojtaba.folentra.core.security.preferences.SensitivePreferences
import java.time.Clock
import java.time.ZoneId

class BackgroundJobSettingsManager(
    private val preferences: SensitivePreferences,
    private val scheduler: BackgroundTaskScheduler,
    private val featureFlags: FeatureFlagEvaluator,
    private val scheduleCalculator: MonthlySummaryScheduleCalculator = MonthlySummaryScheduleCalculator(),
) {
    suspend fun state(): BackgroundJobSettingsState {
        val intentEnabled = preferences.getBoolean(Keys.MonthlySummaryEnabled, defaultValue = false)
        val time = reminderTime()
        val backgroundJobsEnabled = featureFlags.isEnabled(DefaultFeatureFlags.BackgroundJobsEnabled)
        val status = scheduler.status(MonthlySummaryPreparationTask.Id)
        return BackgroundJobSettingsState(
            monthlySummaryEnabled = intentEnabled,
            monthlySummaryTime = time,
            controlsEnabled = backgroundJobsEnabled,
            statusLabel = status.name,
            errorMessage = null,
        )
    }

    suspend fun setMonthlySummaryEnabled(enabled: Boolean): BackgroundJobSettingsState {
        preferences.putBoolean(Keys.MonthlySummaryEnabled, enabled)
        val result = reconcile()
        return state().copy(errorMessage = result.errorMessage)
    }

    suspend fun setMonthlySummaryTime(time: MonthlySummaryReminderTime): BackgroundJobSettingsState {
        preferences.putLong(Keys.MonthlySummaryHour, time.hour.toLong())
        preferences.putLong(Keys.MonthlySummaryMinute, time.minute.toLong())
        val result = reconcile()
        return state().copy(errorMessage = result.errorMessage)
    }

    suspend fun reconcile(): BackgroundJobReconcileResult {
        val intentEnabled = preferences.getBoolean(Keys.MonthlySummaryEnabled, defaultValue = false)
        if (!intentEnabled || !featureFlags.isEnabled(DefaultFeatureFlags.BackgroundJobsEnabled)) {
            return scheduler.cancel(MonthlySummaryPreparationTask.Id).toReconcileResult()
        }
        val scheduled = scheduleCalculator.nextInput(reminderTime())
        return scheduler.enqueue(
            MonthlySummaryPreparationTask.scheduledTask(
                input = scheduled.input,
                initialDelay = scheduled.initialDelay,
            ),
        ).toReconcileResult()
    }

    private suspend fun reminderTime(): MonthlySummaryReminderTime {
        val hour = preferences.getLong(Keys.MonthlySummaryHour)?.toInt() ?: MonthlySummaryReminderTime.Default.hour
        val minute = preferences.getLong(Keys.MonthlySummaryMinute)?.toInt() ?: MonthlySummaryReminderTime.Default.minute
        return runCatching { MonthlySummaryReminderTime(hour, minute) }.getOrDefault(MonthlySummaryReminderTime.Default)
    }

    private fun SchedulerResult.toReconcileResult(): BackgroundJobReconcileResult =
        when (this) {
            SchedulerResult.Success -> BackgroundJobReconcileResult.Success
            is SchedulerResult.Failure -> BackgroundJobReconcileResult.Failure(message)
        }

    private object Keys {
        val MonthlySummaryEnabled = BooleanPreferenceKey("background_monthly_summary_enabled")
        val MonthlySummaryHour = LongPreferenceKey("background_monthly_summary_hour")
        val MonthlySummaryMinute = LongPreferenceKey("background_monthly_summary_minute")
    }
}

data class BackgroundJobSettingsState(
    val monthlySummaryEnabled: Boolean = false,
    val monthlySummaryTime: MonthlySummaryReminderTime = MonthlySummaryReminderTime.Default,
    val controlsEnabled: Boolean = false,
    val statusLabel: String = "NotScheduled",
    val errorMessage: String? = null,
) {
    val monthlySummarySupportingText: String
        get() = when {
            !controlsEnabled -> "Background jobs are disabled by feature flag. Your preference is saved."
            monthlySummaryEnabled -> "Scheduled around ${monthlySummaryTime.displayLabel()}. Current status: $statusLabel."
            else -> "Prepare private monthly insights in the background. Current status: $statusLabel."
        }
}

sealed interface BackgroundJobReconcileResult {
    val errorMessage: String?

    data object Success : BackgroundJobReconcileResult {
        override val errorMessage: String? = null
    }

    data class Failure(
        val message: String,
    ) : BackgroundJobReconcileResult {
        override val errorMessage: String = message
    }
}