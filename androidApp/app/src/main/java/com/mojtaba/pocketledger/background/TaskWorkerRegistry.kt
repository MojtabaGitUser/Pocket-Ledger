package com.mojtaba.pocketledger.background

import androidx.work.ListenableWorker
import com.mojtaba.pocketledger.core.background.BackgroundTaskId

class TaskWorkerRegistry(
    private val workerClasses: Map<BackgroundTaskId, Class<out ListenableWorker>>,
) {
    fun workerClassFor(taskId: BackgroundTaskId): Class<out ListenableWorker>? =
        workerClasses[taskId]

    companion object {
        val Empty = TaskWorkerRegistry(emptyMap())
    }
}
