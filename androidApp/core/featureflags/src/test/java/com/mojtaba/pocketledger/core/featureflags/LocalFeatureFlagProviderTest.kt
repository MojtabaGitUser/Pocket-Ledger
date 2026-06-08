package com.mojtaba.pocketledger.core.featureflags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalFeatureFlagProviderTest {
    @Test
    fun missingOverrideReturnsDefaultValue() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("missing_override"),
            defaultValue = true,
            description = "Test default fallback.",
        )
        val provider = LocalFeatureFlagProvider()

        assertTrue(provider.isEnabled(flag))
    }

    @Test
    fun booleanOverrideCanEnableAndDisableFlag() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("boolean_override"),
            defaultValue = false,
            description = "Test boolean override.",
        )

        assertTrue(
            LocalFeatureFlagProvider(
                mapOf(flag.key to FeatureFlagValue.BooleanValue(true)),
            ).isEnabled(flag),
        )
        assertFalse(
            LocalFeatureFlagProvider(
                mapOf(flag.key to FeatureFlagValue.BooleanValue(false)),
            ).isEnabled(flag),
        )
    }

    @Test
    fun typedOverridesReturnMatchingValues() {
        val intFlag = IntFeatureFlag(
            key = FeatureFlagKey("max_items"),
            defaultValue = 10,
            description = "Test integer flag.",
        )
        val longFlag = LongFeatureFlag(
            key = FeatureFlagKey("timeout_ms"),
            defaultValue = 100L,
            description = "Test long flag.",
        )
        val provider = LocalFeatureFlagProvider(
            mapOf(
                intFlag.key to FeatureFlagValue.IntValue(25),
                longFlag.key to FeatureFlagValue.LongValue(500L),
            ),
        )

        assertEquals(25, provider.valueOf(intFlag))
        assertEquals(500L, provider.valueOf(longFlag))
    }

    @Test
    fun typeMismatchThrows() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("wrong_type"),
            defaultValue = false,
            description = "Test type mismatch.",
        )
        val provider = LocalFeatureFlagProvider(
            mapOf(flag.key to FeatureFlagValue.StringValue("true")),
        )

        assertThrows(IllegalArgumentException::class.java) {
            provider.isEnabled(flag)
        }
    }

    @Test
    fun blankKeyAndDescriptionAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            FeatureFlagKey(" ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            BooleanFeatureFlag(
                key = FeatureFlagKey("missing_description"),
                defaultValue = false,
                description = " ",
            )
        }
    }
}
