package com.mojtaba.pocketledger.core.testing.featureflags

import com.mojtaba.pocketledger.core.featureflags.BooleanFeatureFlag
import com.mojtaba.pocketledger.core.featureflags.FeatureFlag
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagKey
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagProvider
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagValue
import com.mojtaba.pocketledger.core.featureflags.LocalFeatureFlagProvider

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
