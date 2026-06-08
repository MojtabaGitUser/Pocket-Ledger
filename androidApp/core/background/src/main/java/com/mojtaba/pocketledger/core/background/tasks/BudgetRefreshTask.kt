package com.mojtaba.pocketledger.core.background.tasks

import com.mojtaba.pocketledger.core.background.BackgroundTaskId
import com.mojtaba.pocketledger.core.background.RegisteredBackgroundTask

object BudgetRefreshTask {
    val Id = BackgroundTaskId("budget-refresh")

    val Definition = RegisteredBackgroundTask(
        id = Id,
        description = "Future budget summary refresh work",
    )
}
