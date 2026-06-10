package com.mojtaba.pocketledger.core.featureflags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureFlagEvaluatorTest {
    @Test
    fun evaluatorDelegatesTypedValuesToProvider() {
        val flag = StringFeatureFlag(
            key = FeatureFlagKey("test_text"),
            defaultValue = "default",
            description = "Test string flag.",
        )
        val evaluator = FeatureFlagEvaluator(
            LocalFeatureFlagProvider(
                mapOf(flag.key to FeatureFlagValue.StringValue("override")),
            ),
        )

        assertEquals("override", evaluator.valueOf(flag))
    }

    @Test
    fun evaluatorDelegatesBooleanEnabledChecksToProvider() {
        val flag = BooleanFeatureFlag(
            key = FeatureFlagKey("test_boolean"),
            defaultValue = false,
            description = "Test boolean flag.",
        )
        val evaluator = FeatureFlagEvaluator(
            LocalFeatureFlagProvider(
                mapOf(flag.key to FeatureFlagValue.BooleanValue(true)),
            ),
        )

        assertTrue(evaluator.isEnabled(flag))
    }
}
