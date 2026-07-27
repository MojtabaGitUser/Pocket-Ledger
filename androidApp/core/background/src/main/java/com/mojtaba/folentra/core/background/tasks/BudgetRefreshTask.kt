package com.mojtaba.folentra.core.background.tasks

import com.mojtaba.folentra.core.background.BackgroundTaskId
import com.mojtaba.folentra.core.background.RegisteredBackgroundTask

object BudgetRefreshTask {
    val Id = BackgroundTaskId("budget-refresh")

    val Definition = RegisteredBackgroundTask(
        id = Id,
        description = "Future budget summary refresh work",
    )
}
