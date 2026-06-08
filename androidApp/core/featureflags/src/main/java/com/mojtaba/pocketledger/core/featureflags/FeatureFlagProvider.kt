package com.mojtaba.pocketledger.core.featureflags

interface FeatureFlagProvider {
    fun <T> valueOf(flag: FeatureFlag<T>): T

    fun isEnabled(flag: BooleanFeatureFlag): Boolean = valueOf(flag)
}
