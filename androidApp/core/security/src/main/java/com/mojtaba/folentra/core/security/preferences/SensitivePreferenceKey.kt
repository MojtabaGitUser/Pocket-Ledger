package com.mojtaba.folentra.core.security.preferences

sealed interface SensitivePreferenceKey<T> {
    val name: String
}

data class StringPreferenceKey(
    override val name: String,
) : SensitivePreferenceKey<String> {
    init {
        require(name.isNotBlank()) { "Sensitive preference key name must not be blank." }
    }
}

data class BooleanPreferenceKey(
    override val name: String,
) : SensitivePreferenceKey<Boolean> {
    init {
        require(name.isNotBlank()) { "Sensitive preference key name must not be blank." }
    }
}

data class LongPreferenceKey(
    override val name: String,
) : SensitivePreferenceKey<Long> {
    init {
        require(name.isNotBlank()) { "Sensitive preference key name must not be blank." }
    }
}
