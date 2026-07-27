package com.mojtaba.folentra.background

import com.mojtaba.folentra.core.background.SchedulerResult
import com.mojtaba.folentra.core.background.TaskStatus
import com.mojtaba.folentra.core.background.tasks.MonthlySummaryPreparationTask
import com.mojtaba.folentra.core.featureflags.DefaultFeatureFlags
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.folentra.core.security.preferences.InMemorySensitivePreferences
import com.mojtaba.folentra.core.testing.featureflags.FakeFeatureFlagProvider
import com.mojtaba.folentra.core.testing.scheduler.FakeScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundJobSettingsManagerTest {
    @Test
    fun enablingMonthlySummarySchedulesWhenGlobalBackgroundJobsEnabled() = runTest {
        val featureFlags = FakeFeatureFlagProvider().apply { enable(DefaultFeatureFlags.BackgroundJobsEnabled) }
        val scheduler = FakeScheduler()
        val manager = manager(featureFlags = featureFlags, scheduler = scheduler)

        val state = manager.setMonthlySummaryEnabled(true)

        assertTrue(state.monthlySummaryEnabled)
        assertTrue(state.controlsEnabled)
        val task = scheduler.scheduledTask(MonthlySummaryPreparationTask.Id)
        requireNotNull(task)
        assertEquals(MonthlySummaryPreparationTask.Id, task.id)
        assertEquals(TaskStatus.Enqueued, scheduler.status(MonthlySummaryPreparationTask.Id))
    }

    @Test
    fun disablingMonthlySummaryCancelsOnlyMonthlySummaryWork() = runTest {
        val featureFlags = FakeFeatureFlagProvider().apply { enable(DefaultFeatureFlags.BackgroundJobsEnabled) }
        val scheduler = FakeScheduler()
        val manager = manager(featureFlags = featureFlags, scheduler = scheduler)
        manager.setMonthlySummaryEnabled(true)

        val state = manager.setMonthlySummaryEnabled(false)

        assertFalse(state.monthlySummaryEnabled)
        assertEquals(listOf(MonthlySummaryPreparationTask.Id), scheduler.cancelledTaskIds)
        assertEquals(TaskStatus.Cancelled, scheduler.status(MonthlySummaryPreparationTask.Id))
    }

    @Test
    fun globalBackgroundJobsDisabledPreservesUserIntentButDoesNotSchedule() = runTest {
        val featureFlags = FakeFeatureFlagProvider().apply { disable(DefaultFeatureFlags.BackgroundJobsEnabled) }
        val scheduler = FakeScheduler()
        val manager = manager(featureFlags = featureFlags, scheduler = scheduler)

        val state = manager.setMonthlySummaryEnabled(true)

        assertTrue(state.monthlySummaryEnabled)
        assertFalse(state.controlsEnabled)
        assertEquals(emptyList<Any>(), scheduler.enqueuedTasks)
        assertEquals(listOf(MonthlySummaryPreparationTask.Id), scheduler.cancelledTaskIds)
    }

    @Test
    fun schedulerFailureIsReturnedAsUserVisibleError() = runTest {
        val featureFlags = FakeFeatureFlagProvider().apply { enable(DefaultFeatureFlags.BackgroundJobsEnabled) }
        val scheduler = FakeScheduler().apply {
            enqueueResult = SchedulerResult.Failure("Unable to schedule")
        }
        val manager = manager(featureFlags = featureFlags, scheduler = scheduler)

        val state = manager.setMonthlySummaryEnabled(true)

        assertEquals("Unable to schedule", state.errorMessage)
        assertEquals(TaskStatus.NotScheduled, scheduler.status(MonthlySummaryPreparationTask.Id))
    }

    private fun manager(
        featureFlags: FakeFeatureFlagProvider,
        scheduler: FakeScheduler,
    ): BackgroundJobSettingsManager = BackgroundJobSettingsManager(
        preferences = InMemorySensitivePreferences(),
        scheduler = scheduler,
        featureFlags = FeatureFlagEvaluator(featureFlags),
    )
}