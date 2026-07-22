package com.mojtaba.folentra.background

import com.mojtaba.folentra.core.background.BackgroundTaskId
import com.mojtaba.folentra.core.background.tasks.MonthlySummaryPreparationTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskWorkerRegistryTest {
    @Test
    fun resolvesMonthlySummaryPreparationWorker() {
        val registry = TaskWorkerRegistry(
            mapOf(MonthlySummaryPreparationTask.Id to MonthlySummaryPreparationWorker::class.java),
        )

        assertEquals(
            MonthlySummaryPreparationWorker::class.java,
            registry.workerClassFor(MonthlySummaryPreparationTask.Id),
        )
    }

    @Test
    fun returnsNullForUnregisteredTask() {
        val registry = TaskWorkerRegistry.Empty

        assertNull(registry.workerClassFor(BackgroundTaskId("unknown")))
    }
}
