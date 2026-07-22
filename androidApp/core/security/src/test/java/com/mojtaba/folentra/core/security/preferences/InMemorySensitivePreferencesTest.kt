package com.mojtaba.folentra.core.security.preferences

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemorySensitivePreferencesTest {
    @Test
    fun stringValuesCanBeStoredReadAndOverwritten() = runTest {
        val preferences = InMemorySensitivePreferences()
        val key = StringPreferenceKey("session_token")

        preferences.putString(key, "initial")
        preferences.putString(key, "updated")

        assertEquals("updated", preferences.getString(key))
    }

    @Test
    fun booleanValuesCanBeStoredAndRead() = runTest {
        val preferences = InMemorySensitivePreferences()
        val key = BooleanPreferenceKey("biometric_enabled")

        assertFalse(preferences.getBoolean(key))

        preferences.putBoolean(key, true)

        assertTrue(preferences.getBoolean(key))
    }

    @Test
    fun longValuesCanBeStoredAndRead() = runTest {
        val preferences = InMemorySensitivePreferences()
        val key = LongPreferenceKey("last_check")

        assertNull(preferences.getLong(key))

        preferences.putLong(key, 1234L)

        assertEquals(1234L, preferences.getLong(key))
    }

    @Test
    fun removeClearsOneKeyOnly() = runTest {
        val preferences = InMemorySensitivePreferences()
        val stringKey = StringPreferenceKey("token")
        val longKey = LongPreferenceKey("timestamp")
        preferences.putString(stringKey, "secret")
        preferences.putLong(longKey, 99L)

        preferences.remove(stringKey)

        assertNull(preferences.getString(stringKey))
        assertEquals(99L, preferences.getLong(longKey))
    }

    @Test
    fun clearRemovesAllKeys() = runTest {
        val preferences = InMemorySensitivePreferences()
        val stringKey = StringPreferenceKey("token")
        val booleanKey = BooleanPreferenceKey("enabled")
        preferences.putString(stringKey, "secret")
        preferences.putBoolean(booleanKey, true)

        preferences.clear()

        assertNull(preferences.getString(stringKey))
        assertFalse(preferences.getBoolean(booleanKey))
        assertTrue(preferences.storedValues.isEmpty())
    }

    @Test
    fun missingBooleanCanUseCustomDefault() = runTest {
        val preferences = InMemorySensitivePreferences()

        assertTrue(preferences.getBoolean(BooleanPreferenceKey("missing"), defaultValue = true))
    }

    @Test
    fun typedKeySeparationDoesNotCoerceValues() = runTest {
        val preferences = InMemorySensitivePreferences()
        val name = "same_backing_name"
        preferences.putString(StringPreferenceKey(name), "true")

        assertFalse(preferences.getBoolean(BooleanPreferenceKey(name)))
    }

    @Test
    fun defaultKeysHaveUniqueNonBlankNames() {
        DefaultSensitivePreferenceKeys.All.forEach { key ->
            assertTrue(key.name.isNotBlank())
        }

        val keyNames = DefaultSensitivePreferenceKeys.All.map { it.name }
        assertEquals(keyNames.toSet().size, keyNames.size)
    }

    @Test
    fun blankKeyNamesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            StringPreferenceKey(" ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            BooleanPreferenceKey(" ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            LongPreferenceKey(" ")
        }
    }
}
