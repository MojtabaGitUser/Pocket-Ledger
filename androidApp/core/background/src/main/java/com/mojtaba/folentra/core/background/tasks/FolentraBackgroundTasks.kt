package com.mojtaba.folentra.core.background.tasks

import com.mojtaba.folentra.core.background.BackgroundTaskRegistry

object FolentraBackgroundTasks {
    val Registry = BackgroundTaskRegistry(
        tasks = setOf(
            SyncTask.Definition,
            CleanupTask.Definition,
            BudgetRefreshTask.Definition,
            MonthlySummaryPreparationTask.Definition,
        ),
    )
}