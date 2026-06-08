package com.mojtaba.pocketledger.core.featureflags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultFeatureFlagsTest {
    @Test
    fun defaultFlagsHaveNonBlankMetadata() {
        DefaultFeatureFlags.All.forEach { flag ->
            assertTrue(flag.key.value.isNotBlank())
            assertTrue(flag.description.isNotBlank())
        }
    }

    @Test
    fun defaultFlagsDoNotContainDuplicateKeys() {
        val keys = DefaultFeatureFlags.All.map { it.key }

        assertEquals(keys.toSet().size, keys.size)
    }

    @Test
    fun incompleteAndOptionalFeaturesDefaultToDisabled() {
        DefaultFeatureFlags.All
            .filterIsInstance<BooleanFeatureFlag>()
            .forEach { flag ->
                assertFalse(
                    "${flag.key} should default to disabled.",
                    flag.defaultValue,
                )
            }
    }
}
