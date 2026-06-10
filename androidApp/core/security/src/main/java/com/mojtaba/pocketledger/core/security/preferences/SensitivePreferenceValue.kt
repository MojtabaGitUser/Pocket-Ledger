package com.mojtaba.pocketledger.core.security.preferences

sealed interface SensitivePreferenceValue {
    data class StringValue(val value: String) : SensitivePreferenceValue

    data class BooleanValue(val value: Boolean) : SensitivePreferenceValue

    data class LongValue(val value: Long) : SensitivePreferenceValue
}
