package com.mojtaba.folentra.core.background.tasks

import com.mojtaba.folentra.core.background.BackgroundTaskId
import com.mojtaba.folentra.core.background.RegisteredBackgroundTask

object SyncTask {
    val Id = BackgroundTaskId("sync")

    val Definition = RegisteredBackgroundTask(
        id = Id,
        description = "Future offline-first sync work",
    )
}
