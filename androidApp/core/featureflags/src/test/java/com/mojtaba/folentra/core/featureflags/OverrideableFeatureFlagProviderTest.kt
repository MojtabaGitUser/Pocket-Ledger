package com.mojtaba.folentra.core.featureflags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OverrideableFeatureFlagProviderTest {
    @Test
    fun missingOverrideFallsBackToBaseProvider() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("fallback_flag"),
            defaultValue = true,
            description = "Test fallback flag.",
        )
        val provider = OverrideableFeatureFlagProvider()

        assertTrue(provider.isEnabled(flag))
        assertTrue(provider.overridesSnapshot().isEmpty())
    }

    @Test
    fun setOverrideChangesReturnedValue() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("overridden_flag"),
            defaultValue = false,
            description = "Test overridden flag.",
        )
        val provider = OverrideableFeatureFlagProvider()

        provider.setOverride(flag, true)

        assertTrue(provider.isEnabled(flag))
        assertEquals(FeatureFlagValue.BooleanValue(true), provider.overrideOf(flag))
    }

    @Test
    fun clearOverrideRestoresDefaultValue() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("clearable_flag"),
            defaultValue = false,
            description = "Test clearable flag.",
        )
        val provider = OverrideableFeatureFlagProvider()

        provider.setOverride(flag, true)
        provider.clearOverride(flag)

        assertFalse(provider.isEnabled(flag))
        assertTrue(provider.overridesSnapshot().isEmpty())
    }

    @Test
    fun clearAllOverridesRemovesEveryOverride() {
        val first = BooleanFeatureFlag(
            key = FeatureFlagKey("first_flag"),
            defaultValue = false,
            description = "Test first flag.",
        )
        val second = BooleanFeatureFlag(
            key = FeatureFlagKey("second_flag"),
            defaultValue = true,
            description = "Test second flag.",
        )
        val provider = OverrideableFeatureFlagProvider()

        provider.setOverride(first, true)
        provider.setOverride(second, false)
        provider.clearAllOverrides()

        assertFalse(provider.isEnabled(first))
        assertTrue(provider.isEnabled(second))
        assertTrue(provider.overridesSnapshot().isEmpty())
    }

    @Test
    fun typeMismatchThrows() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("wrong_override_type"),
            defaultValue = false,
            description = "Test wrong override type.",
        )
        val store = InMemoryFeatureFlagOverrideStore(
            mapOf(flag.key to FeatureFlagValue.StringValue("true")),
        )
        val provider = OverrideableFeatureFlagProvider(overrideStore = store)

        assertThrows(IllegalArgumentException::class.java) {
            provider.isEnabled(flag)
        }
    }
}