package com.mojtaba.pocketledger.core.featureflags

import android.content.SharedPreferences

class OverrideableFeatureFlagProvider(
    private val baseProvider: FeatureFlagProvider = LocalFeatureFlagProvider(),
    private val overrideStore: FeatureFlagOverrideStore = InMemoryFeatureFlagOverrideStore(),
) : FeatureFlagProvider {
    override fun <T> valueOf(flag: FeatureFlag<T>): T {
        val override = overrideStore.snapshot()[flag.key] ?: return baseProvider.valueOf(flag)
        return flag.valueFrom(override)
            ?: throw IllegalArgumentException(
                "Feature flag '${flag.key}' expects ${flag.valueTypeName} but received ${override.typeName}.",
            )
    }

    fun <T> setOverride(flag: FeatureFlag<T>, value: T) {
        overrideStore.set(flag.key, flag.valueTo(value))
    }

    fun clearOverride(flag: FeatureFlag<*>) {
        overrideStore.clear(flag.key)
    }

    fun clearAllOverrides() {
        overrideStore.clearAll()
    }

    fun overrideOf(flag: FeatureFlag<*>): FeatureFlagValue? = overrideStore.snapshot()[flag.key]

    fun overridesSnapshot(): Map<FeatureFlagKey, FeatureFlagValue> = overrideStore.snapshot()
}

interface FeatureFlagOverrideStore {
    fun snapshot(): Map<FeatureFlagKey, FeatureFlagValue>
    fun set(key: FeatureFlagKey, value: FeatureFlagValue)
    fun clear(key: FeatureFlagKey)
    fun clearAll()
}

class InMemoryFeatureFlagOverrideStore(
    initialOverrides: Map<FeatureFlagKey, FeatureFlagValue> = emptyMap(),
) : FeatureFlagOverrideStore {
    private val overrides = initialOverrides.toMutableMap()

    override fun snapshot(): Map<FeatureFlagKey, FeatureFlagValue> = overrides.toMap()

    override fun set(key: FeatureFlagKey, value: FeatureFlagValue) {
        overrides[key] = value
    }

    override fun clear(key: FeatureFlagKey) {
        overrides.remove(key)
    }

    override fun clearAll() {
        overrides.clear()
    }
}

class SharedPreferencesFeatureFlagOverrideStore(
    private val sharedPreferences: SharedPreferences,
) : FeatureFlagOverrideStore {
    override fun snapshot(): Map<FeatureFlagKey, FeatureFlagValue> =
        sharedPreferences.all.mapNotNull { (rawKey, rawValue) ->
            val encoded = rawValue as? String ?: return@mapNotNull null
            parseValue(encoded)?.let { value -> FeatureFlagKey(rawKey) to value }
        }.toMap()

    override fun set(key: FeatureFlagKey, value: FeatureFlagValue) {
        sharedPreferences.edit().putString(key.value, value.encode()).apply()
    }

    override fun clear(key: FeatureFlagKey) {
        sharedPreferences.edit().remove(key.value).apply()
    }

    override fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    private fun FeatureFlagValue.encode(): String = when (this) {
        is FeatureFlagValue.BooleanValue -> "boolean:$value"
        is FeatureFlagValue.StringValue -> "string:$value"
        is FeatureFlagValue.IntValue -> "int:$value"
        is FeatureFlagValue.LongValue -> "long:$value"
    }

    private fun parseValue(encoded: String): FeatureFlagValue? {
        val separatorIndex = encoded.indexOf(':')
        if (separatorIndex <= 0) return null
        val type = encoded.substring(0, separatorIndex)
        val rawValue = encoded.substring(separatorIndex + 1)
        return when (type) {
            "boolean" -> rawValue.toBooleanStrictOrNull()?.let(FeatureFlagValue::BooleanValue)
            "string" -> FeatureFlagValue.StringValue(rawValue)
            "int" -> rawValue.toIntOrNull()?.let(FeatureFlagValue::IntValue)
            "long" -> rawValue.toLongOrNull()?.let(FeatureFlagValue::LongValue)
            else -> null
        }
    }
}