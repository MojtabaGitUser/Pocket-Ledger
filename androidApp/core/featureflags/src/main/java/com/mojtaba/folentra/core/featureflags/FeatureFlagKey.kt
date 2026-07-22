package com.mojtaba.folentra.core.featureflags

@JvmInline
value class FeatureFlagKey(val value: String) {
    init {
        require(value.isNotBlank()) { "Feature flag key must not be blank." }
    }

    override fun toString(): String = value
}
