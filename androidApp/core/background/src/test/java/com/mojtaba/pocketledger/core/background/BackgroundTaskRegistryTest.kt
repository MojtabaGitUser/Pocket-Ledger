package com.mojtaba.pocketledger.core.background

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundTaskRegistryTest {
    @Test
    fun registryReturnsRegisteredTaskById() {
        val task = RegisteredBackgroundTask(
            id = BackgroundTaskId("cleanup"),
            description = "Cleanup",
        )
        val registry = BackgroundTaskRegistry(setOf(task))

        assertTrue(registry.contains(task.id))
        assertEquals(task, registry.requireTask(task.id))
        assertEquals(setOf(task), registry.registeredTasks())
    }

    @Test
    fun registryRejectsUnknownTask() {
        val registry = BackgroundTaskRegistry(emptySet())

        assertFalse(registry.contains(BackgroundTaskId("missing")))
        assertThrows(IllegalStateException::class.java) {
            registry.requireTask(BackgroundTaskId("missing"))
        }
    }
}
