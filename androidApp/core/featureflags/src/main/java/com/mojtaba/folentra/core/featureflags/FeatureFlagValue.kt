package com.mojtaba.folentra.core.featureflags

sealed interface FeatureFlagValue {
    val typeName: String

    data class BooleanValue(val value: Boolean) : FeatureFlagValue {
        override val typeName: String = "Boolean"
    }

    data class StringValue(val value: String) : FeatureFlagValue {
        override val typeName: String = "String"
    }

    data class IntValue(val value: Int) : FeatureFlagValue {
        override val typeName: String = "Int"
    }

    data class LongValue(val value: Long) : FeatureFlagValue {
        override val typeName: String = "Long"
    }
}
