package com.mojtaba.folentra.core.testing.featureflags

import com.mojtaba.folentra.core.featureflags.BooleanFeatureFlag
import com.mojtaba.folentra.core.featureflags.FeatureFlag
import com.mojtaba.folentra.core.featureflags.FeatureFlagKey
import com.mojtaba.folentra.core.featureflags.FeatureFlagProvider
import com.mojtaba.folentra.core.featureflags.FeatureFlagValue
import com.mojtaba.folentra.core.featureflags.LocalFeatureFlagProvider

class FakeFeatureFlagProvider(
    initialOverrides: Map<FeatureFlagKey, FeatureFlagValue> = emptyMap(),
) : FeatureFlagProvider {
    private val overrides = initialOverrides.toMutableMap()

    val configuredOverrides: Map<FeatureFlagKey, FeatureFlagValue>
        get() = overrides.toMap()

    override fun <T> valueOf(flag: FeatureFlag<T>): T =
        LocalFeatureFlagProvider(overrides).valueOf(flag)

    fun enable(flag: BooleanFeatureFlag) {
        set(flag, true)
    }

    fun disable(flag: BooleanFeatureFlag) {
        set(flag, false)
    }

    fun <T> set(
        flag: FeatureFlag<T>,
        value: T,
    ) {
        overrides[flag.key] = flag.valueTo(value)
    }

    fun clear() {
        overrides.clear()
    }
}
