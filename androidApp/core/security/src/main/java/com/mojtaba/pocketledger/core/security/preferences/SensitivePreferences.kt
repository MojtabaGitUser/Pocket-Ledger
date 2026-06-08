package com.mojtaba.pocketledger.core.security.preferences

interface SensitivePreferences {
    suspend fun putString(
        key: StringPreferenceKey,
        value: String,
    )

    suspend fun getString(key: StringPreferenceKey): String?

    suspend fun putBoolean(
        key: BooleanPreferenceKey,
        value: Boolean,
    )

    suspend fun getBoolean(
        key: BooleanPreferenceKey,
        defaultValue: Boolean = false,
    ): Boolean

    suspend fun putLong(
        key: LongPreferenceKey,
        value: Long,
    )

    suspend fun getLong(key: LongPreferenceKey): Long?

    suspend fun remove(key: SensitivePreferenceKey<*>)

    suspend fun clear()
}
