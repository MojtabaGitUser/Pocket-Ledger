package com.mojtaba.pocketledger.core.security.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EncryptedSensitivePreferencesTest {
    private lateinit var context: Context
    private lateinit var fileName: String
    private lateinit var preferences: EncryptedSensitivePreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fileName = "test_sensitive_prefs_${UUID.randomUUID()}"
        preferences = EncryptedSensitivePreferences(
            context = context,
            fileName = fileName,
        )
    }

    @After
    fun tearDown() {
        runBlocking {
            preferences.clear()
        }
        context.deleteSharedPreferences(fileName)
    }

    @Test
    fun encryptedPreferencesCanWriteAndReadString() = runBlocking {
        val key = StringPreferenceKey("session_token")

        preferences.putString(key, "secret-token")

        assertEquals("secret-token", preferences.getString(key))
    }

    @Test
    fun encryptedPreferencesCanWriteAndReadBoolean() = runBlocking {
        val key = BooleanPreferenceKey("biometric_enabled")

        assertFalse(preferences.getBoolean(key))

        preferences.putBoolean(key, true)

        assertTrue(preferences.getBoolean(key))
    }

    @Test
    fun encryptedPreferencesCanWriteAndReadLong() = runBlocking {
        val key = LongPreferenceKey("last_security_check")

        assertNull(preferences.getLong(key))

        preferences.putLong(key, 9876L)

        assertEquals(9876L, preferences.getLong(key))
    }

    @Test
    fun removeClearsOneEncryptedValueOnly() = runBlocking {
        val stringKey = StringPreferenceKey("credential")
        val longKey = LongPreferenceKey("timestamp")
        preferences.putString(stringKey, "credential-id")
        preferences.putLong(longKey, 42L)

        preferences.remove(stringKey)

        assertNull(preferences.getString(stringKey))
        assertEquals(42L, preferences.getLong(longKey))
    }

    @Test
    fun clearRemovesEncryptedValues() = runBlocking {
        val stringKey = StringPreferenceKey("credential")
        val booleanKey = BooleanPreferenceKey("enabled")
        preferences.putString(stringKey, "credential-id")
        preferences.putBoolean(booleanKey, true)

        preferences.clear()

        assertNull(preferences.getString(stringKey))
        assertFalse(preferences.getBoolean(booleanKey))
    }

    @Test
    fun encryptedValuesSurviveNewInstanceWithSameFileName() = runBlocking {
        val key = StringPreferenceKey("session_token")
        preferences.putString(key, "persisted-secret")

        val newInstance = EncryptedSensitivePreferences(
            context = context,
            fileName = fileName,
        )

        assertEquals("persisted-secret", newInstance.getString(key))
    }
}
