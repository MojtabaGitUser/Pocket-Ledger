package com.mojtaba.folentra.core.background.tasks

import com.mojtaba.folentra.core.background.BackgroundTaskId
import com.mojtaba.folentra.core.background.RegisteredBackgroundTask

object CleanupTask {
    val Id = BackgroundTaskId("cleanup")

    val Definition = RegisteredBackgroundTask(
        id = Id,
        description = "Future local maintenance work",
    )
}
