package com.mojtaba.folentra.background

import androidx.work.ListenableWorker
import com.mojtaba.folentra.core.background.BackgroundTaskId

class TaskWorkerRegistry(
    private val workerClasses: Map<BackgroundTaskId, Class<out ListenableWorker>>,
) {
    fun workerClassFor(taskId: BackgroundTaskId): Class<out ListenableWorker>? =
        workerClasses[taskId]

    companion object {
        val Empty = TaskWorkerRegistry(emptyMap())
    }
}
