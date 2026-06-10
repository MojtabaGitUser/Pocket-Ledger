package com.mojtaba.pocketledger.core.background.tasks

import com.mojtaba.pocketledger.core.background.BackgroundTaskRegistry

object PocketLedgerBackgroundTasks {
    val Registry = BackgroundTaskRegistry(
        tasks = setOf(
            SyncTask.Definition,
            CleanupTask.Definition,
            BudgetRefreshTask.Definition,
        ),
    )
}
