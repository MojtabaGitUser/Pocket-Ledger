package com.mojtaba.folentra.core.featureflags

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
    fun implementedLocalAiFeaturesDefaultToEnabled() {
        assertTrue(DefaultFeatureFlags.SemanticSearchEnabled.defaultValue)
        assertTrue(DefaultFeatureFlags.AiInsightsEnabled.defaultValue)
        assertTrue(DefaultFeatureFlags.SmartAutofillEnabled.defaultValue)
    }

    @Test
    fun incompleteAndOptionalFeaturesDefaultToDisabled() {
        listOf(
            DefaultFeatureFlags.PasskeyAccountFlowEnabled,
            DefaultFeatureFlags.CloudSyncEnabled,
            DefaultFeatureFlags.BackgroundJobsEnabled,
            DefaultFeatureFlags.DemoDataToolsEnabled,
            DefaultFeatureFlags.ScreenshotTestingEnabled,
        ).forEach { flag ->
            assertFalse(
                "${flag.key} should default to disabled.",
                flag.defaultValue,
            )
        }
    }
}
