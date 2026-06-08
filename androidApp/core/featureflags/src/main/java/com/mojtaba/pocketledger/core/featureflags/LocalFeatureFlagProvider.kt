package com.mojtaba.pocketledger.core.featureflags

class LocalFeatureFlagProvider(
    private val overrides: Map<FeatureFlagKey, FeatureFlagValue> = emptyMap(),
) : FeatureFlagProvider {
    override fun <T> valueOf(flag: FeatureFlag<T>): T {
        val override = overrides[flag.key] ?: return flag.defaultValue
        return flag.valueFrom(override)
            ?: throw IllegalArgumentException(
                "Feature flag '${flag.key}' expects ${flag.valueTypeName} but received ${override.typeName}.",
            )
    }
}
