package com.mojtaba.pocketledger.core.background.tasks

import com.mojtaba.pocketledger.core.background.BackgroundTaskId
import com.mojtaba.pocketledger.core.background.RegisteredBackgroundTask

object SyncTask {
    val Id = BackgroundTaskId("sync")

    val Definition = RegisteredBackgroundTask(
        id = Id,
        description = "Future offline-first sync work",
    )
}
