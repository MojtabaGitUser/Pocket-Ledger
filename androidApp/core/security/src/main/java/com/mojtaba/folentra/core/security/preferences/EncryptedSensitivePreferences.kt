@file:Suppress("DEPRECATION")

package com.mojtaba.folentra.core.security.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EncryptedSensitivePreferences(
    context: Context,
    private val fileName: String = DEFAULT_FILE_NAME,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SensitivePreferences {
    private val applicationContext = context.applicationContext

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            applicationContext,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun putString(
        key: StringPreferenceKey,
        value: String,
    ) {
        edit {
            putString(key.name, value)
        }
    }

    override suspend fun getString(key: StringPreferenceKey): String? =
        read {
            getString(key.name, null)
        }

    override suspend fun putBoolean(
        key: BooleanPreferenceKey,
        value: Boolean,
    ) {
        edit {
            putBoolean(key.name, value)
        }
    }

    override suspend fun getBoolean(
        key: BooleanPreferenceKey,
        defaultValue: Boolean,
    ): Boolean =
        read {
            if (contains(key.name)) {
                getBoolean(key.name, defaultValue)
            } else {
                defaultValue
            }
        }

    override suspend fun putLong(
        key: LongPreferenceKey,
        value: Long,
    ) {
        edit {
            putLong(key.name, value)
        }
    }

    override suspend fun getLong(key: LongPreferenceKey): Long? =
        read {
            if (contains(key.name)) {
                getLong(key.name, 0L)
            } else {
                null
            }
        }

    override suspend fun remove(key: SensitivePreferenceKey<*>) {
        edit {
            remove(key.name)
        }
    }

    override suspend fun clear() {
        edit {
            clear()
        }
    }

    private suspend fun edit(operation: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
        withContext(dispatcher) {
            val committed = sharedPreferences.edit()
                .operation()
                .commit()
            check(committed) { "Unable to write sensitive preferences." }
        }
    }

    private suspend fun <T> read(operation: SharedPreferences.() -> T): T =
        withContext(dispatcher) {
            try {
                sharedPreferences.operation()
            } catch (exception: ClassCastException) {
                throw IllegalStateException("Stored sensitive preference has unexpected type.", exception)
            } catch (exception: SecurityException) {
                throw IllegalStateException("Unable to read sensitive preferences.", exception)
            }
        }

    companion object {
        const val DEFAULT_FILE_NAME = "folentra_sensitive_prefs"
    }
}
