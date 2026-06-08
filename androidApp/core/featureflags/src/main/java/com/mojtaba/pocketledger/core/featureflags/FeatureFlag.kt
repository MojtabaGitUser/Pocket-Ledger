package com.mojtaba.pocketledger.core.featureflags

sealed interface FeatureFlag<T> {
    val key: FeatureFlagKey
    val defaultValue: T
    val description: String
    val valueTypeName: String

    fun valueFrom(featureFlagValue: FeatureFlagValue): T?

    fun valueTo(value: T): FeatureFlagValue
}

data class BooleanFeatureFlag(
    override val key: FeatureFlagKey,
    override val defaultValue: Boolean,
    override val description: String,
) : FeatureFlag<Boolean> {
    init {
        require(description.isNotBlank()) { "Feature flag description must not be blank." }
    }

    override val valueTypeName: String = "Boolean"

    override fun valueFrom(featureFlagValue: FeatureFlagValue): Boolean? =
        (featureFlagValue as? FeatureFlagValue.BooleanValue)?.value

    override fun valueTo(value: Boolean): FeatureFlagValue =
        FeatureFlagValue.BooleanValue(value)
}

data class StringFeatureFlag(
    override val key: FeatureFlagKey,
    override val defaultValue: String,
    override val description: String,
) : FeatureFlag<String> {
    init {
        require(description.isNotBlank()) { "Feature flag description must not be blank." }
    }

    override val valueTypeName: String = "String"

    override fun valueFrom(featureFlagValue: FeatureFlagValue): String? =
        (featureFlagValue as? FeatureFlagValue.StringValue)?.value

    override fun valueTo(value: String): FeatureFlagValue =
        FeatureFlagValue.StringValue(value)
}

data class IntFeatureFlag(
    override val key: FeatureFlagKey,
    override val defaultValue: Int,
    override val description: String,
) : FeatureFlag<Int> {
    init {
        require(description.isNotBlank()) { "Feature flag description must not be blank." }
    }

    override val valueTypeName: String = "Int"

    override fun valueFrom(featureFlagValue: FeatureFlagValue): Int? =
        (featureFlagValue as? FeatureFlagValue.IntValue)?.value

    override fun valueTo(value: Int): FeatureFlagValue =
        FeatureFlagValue.IntValue(value)
}

data class LongFeatureFlag(
    override val key: FeatureFlagKey,
    override val defaultValue: Long,
    override val description: String,
) : FeatureFlag<Long> {
    init {
        require(description.isNotBlank()) { "Feature flag description must not be blank." }
    }

    override val valueTypeName: String = "Long"

    override fun valueFrom(featureFlagValue: FeatureFlagValue): Long? =
        (featureFlagValue as? FeatureFlagValue.LongValue)?.value

    override fun valueTo(value: Long): FeatureFlagValue =
        FeatureFlagValue.LongValue(value)
}
