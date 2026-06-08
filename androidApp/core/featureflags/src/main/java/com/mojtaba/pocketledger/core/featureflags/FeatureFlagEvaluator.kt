package com.mojtaba.pocketledger.core.featureflags

class FeatureFlagEvaluator(
    private val provider: FeatureFlagProvider,
) {
    fun <T> valueOf(flag: FeatureFlag<T>): T = provider.valueOf(flag)

    fun isEnabled(flag: BooleanFeatureFlag): Boolean = provider.isEnabled(flag)
}
