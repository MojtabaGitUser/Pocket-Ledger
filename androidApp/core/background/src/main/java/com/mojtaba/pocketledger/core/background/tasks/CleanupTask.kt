package com.mojtaba.pocketledger.core.background.tasks

import com.mojtaba.pocketledger.core.background.BackgroundTaskId
import com.mojtaba.pocketledger.core.background.RegisteredBackgroundTask

object CleanupTask {
    val Id = BackgroundTaskId("cleanup")

    val Definition = RegisteredBackgroundTask(
        id = Id,
        description = "Future local maintenance work",
    )
}
