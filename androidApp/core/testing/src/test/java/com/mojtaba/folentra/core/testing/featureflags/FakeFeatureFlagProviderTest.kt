package com.mojtaba.folentra.core.testing.featureflags

import com.mojtaba.folentra.core.featureflags.BooleanFeatureFlag
import com.mojtaba.folentra.core.featureflags.FeatureFlagKey
import com.mojtaba.folentra.core.featureflags.FeatureFlagValue
import com.mojtaba.folentra.core.featureflags.StringFeatureFlag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeFeatureFlagProviderTest {
    @Test
    fun missingOverrideFallsBackToDefault() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("defaulted"),
            defaultValue = true,
            description = "Test default fallback.",
        )

        assertTrue(FakeFeatureFlagProvider().isEnabled(flag))
    }

    @Test
    fun enableAndDisableOverrideBooleanFlag() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("toggle"),
            defaultValue = false,
            description = "Test fake toggle.",
        )
        val provider = FakeFeatureFlagProvider()

        provider.enable(flag)
        assertTrue(provider.isEnabled(flag))

        provider.disable(flag)
        assertFalse(provider.isEnabled(flag))
    }

    @Test
    fun setRecordsTypedOverride() {
        val flag = StringFeatureFlag(
            key = FeatureFlagKey("copy"),
            defaultValue = "default",
            description = "Test typed fake override.",
        )
        val provider = FakeFeatureFlagProvider()

        provider.set(flag, "override")

        assertEquals("override", provider.valueOf(flag))
        assertEquals(
            mapOf(flag.key to FeatureFlagValue.StringValue("override")),
            provider.configuredOverrides,
        )
    }

    @Test
    fun clearRemovesOverrides() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("clearable"),
            defaultValue = false,
            description = "Test fake clear.",
        )
        val provider = FakeFeatureFlagProvider()
        provider.enable(flag)

        provider.clear()

        assertFalse(provider.isEnabled(flag))
        assertTrue(provider.configuredOverrides.isEmpty())
    }
}
