package com.mojtaba.pocketledger.core.security.preferences

class InMemorySensitivePreferences(
    initialValues: Map<String, SensitivePreferenceValue> = emptyMap(),
) : SensitivePreferences {
    private val values = initialValues.toMutableMap()

    val storedValues: Map<String, SensitivePreferenceValue>
        get() = values.toMap()

    override suspend fun putString(
        key: StringPreferenceKey,
        value: String,
    ) {
        values[key.name] = SensitivePreferenceValue.StringValue(value)
    }

    override suspend fun getString(key: StringPreferenceKey): String? =
        (values[key.name] as? SensitivePreferenceValue.StringValue)?.value

    override suspend fun putBoolean(
        key: BooleanPreferenceKey,
        value: Boolean,
    ) {
        values[key.name] = SensitivePreferenceValue.BooleanValue(value)
    }

    override suspend fun getBoolean(
        key: BooleanPreferenceKey,
        defaultValue: Boolean,
    ): Boolean =
        (values[key.name] as? SensitivePreferenceValue.BooleanValue)?.value ?: defaultValue

    override suspend fun putLong(
        key: LongPreferenceKey,
        value: Long,
    ) {
        values[key.name] = SensitivePreferenceValue.LongValue(value)
    }

    override suspend fun getLong(key: LongPreferenceKey): Long? =
        (values[key.name] as? SensitivePreferenceValue.LongValue)?.value

    override suspend fun remove(key: SensitivePreferenceKey<*>) {
        values -= key.name
    }

    override suspend fun clear() {
        values.clear()
    }
}
