package com.mojtaba.folentra.core.security.applock

import com.mojtaba.folentra.core.security.preferences.DefaultSensitivePreferenceKeys
import com.mojtaba.folentra.core.security.preferences.InMemorySensitivePreferences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLockManagerTest {
    @Test
    fun appLockDisabledOpensUnlocked() = runTest {
        val manager = appLockManager()

        manager.initialize()

        assertEquals(AppLockStatus.Unlocked, manager.state.value.status)
        assertFalse(manager.state.value.isEnabled)
        assertTrue(manager.state.value.isContentVisible)
    }

    @Test
    fun appLockEnabledStartsLocked() = runTest {
        val preferences = InMemorySensitivePreferences()
        preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, true)
        val manager = appLockManager(preferences = preferences)

        manager.initialize()

        assertEquals(AppLockStatus.Locked, manager.state.value.status)
        assertTrue(manager.state.value.isEnabled)
        assertFalse(manager.state.value.isContentVisible)
    }

    @Test
    fun successfulAuthenticationUnlocksProtectedContent() = runTest {
        val preferences = InMemorySensitivePreferences()
        preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, true)
        val manager = appLockManager(
            preferences = preferences,
            authenticator = FakeAppLockAuthenticator(AppLockAuthenticationResult.Success),
        )
        manager.initialize()

        manager.unlock()

        assertEquals(AppLockStatus.Unlocked, manager.state.value.status)
        assertTrue(manager.state.value.isContentVisible)
    }

    @Test
    fun cancelledAuthenticationKeepsContentLocked() = runTest {
        val preferences = InMemorySensitivePreferences()
        preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, true)
        val manager = appLockManager(
            preferences = preferences,
            authenticator = FakeAppLockAuthenticator(AppLockAuthenticationResult.Cancelled),
        )
        manager.initialize()

        manager.unlock()

        assertEquals(AppLockStatus.Locked, manager.state.value.status)
        assertEquals(AppLockMessage.AuthenticationCancelled, manager.state.value.message)
        assertFalse(manager.state.value.isContentVisible)
    }

    @Test
    fun failedAuthenticationKeepsContentLocked() = runTest {
        val preferences = InMemorySensitivePreferences()
        preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, true)
        val manager = appLockManager(
            preferences = preferences,
            authenticator = FakeAppLockAuthenticator(AppLockAuthenticationResult.Failed),
        )
        manager.initialize()

        manager.unlock()

        assertEquals(AppLockStatus.Locked, manager.state.value.status)
        assertEquals(AppLockMessage.AuthenticationFailed, manager.state.value.message)
        assertFalse(manager.state.value.isContentVisible)
    }

    @Test
    fun unsupportedAuthenticatorCannotEnableAppLock() = runTest {
        val preferences = InMemorySensitivePreferences()
        val manager = appLockManager(
            preferences = preferences,
            authenticator = FakeAppLockAuthenticator(
                availability = AppLockAvailability.Unavailable(AppLockUnavailableReason.NoHardware),
            ),
        )

        manager.initialize()
        val enabled = manager.setAppLockEnabled(true)

        assertFalse(enabled)
        assertFalse(preferences.getBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled))
        assertEquals(AppLockStatus.Unlocked, manager.state.value.status)
        assertTrue(manager.state.value.isContentVisible)
    }

    @Test
    fun returningFromBackgroundLocksWhenEnabled() = runTest {
        val manager = appLockManager(authenticator = FakeAppLockAuthenticator(AppLockAuthenticationResult.Success))
        manager.initialize()
        assertTrue(manager.setAppLockEnabled(true))

        manager.onAppForegrounded()

        assertEquals(AppLockStatus.Locked, manager.state.value.status)
        assertFalse(manager.state.value.isContentVisible)
    }

    @Test
    fun settingTogglePersistsEnabledAndDisabledState() = runTest {
        val preferences = InMemorySensitivePreferences()
        val manager = appLockManager(
            preferences = preferences,
            authenticator = FakeAppLockAuthenticator(AppLockAuthenticationResult.Success),
        )
        manager.initialize()

        assertTrue(manager.setAppLockEnabled(true))
        assertTrue(preferences.getBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled))

        assertTrue(manager.setAppLockEnabled(false))
        assertFalse(preferences.getBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled))
        assertEquals(AppLockStatus.Unlocked, manager.state.value.status)
    }

    @Test
    fun enablingAuthenticationKeepsSettingsContentVisible() = runTest {
        val authenticationStarted = CompletableDeferred<Unit>()
        val authenticationResult = CompletableDeferred<AppLockAuthenticationResult>()
        val manager = AppLockManager(
            preferences = InMemorySensitivePreferences(),
            authenticator = object : AppLockAuthenticator {
                override fun availability(): AppLockAvailability = AppLockAvailability.Available

                override suspend fun authenticate(): AppLockAuthenticationResult {
                    authenticationStarted.complete(Unit)
                    return authenticationResult.await()
                }
            },
        )
        manager.initialize()

        val enabling = async { manager.setAppLockEnabled(true) }
        authenticationStarted.await()

        assertEquals(AppLockStatus.Authenticating, manager.state.value.status)
        assertFalse(manager.state.value.isEnabled)
        assertTrue(manager.state.value.isContentVisible)

        authenticationResult.complete(AppLockAuthenticationResult.Success)
        assertTrue(enabling.await())
    }

    @Test
    fun cancelledEnableAuthenticationDoesNotLeaveSpinnerActive() = runTest {
        val authenticationStarted = CompletableDeferred<Unit>()
        val manager = AppLockManager(
            preferences = InMemorySensitivePreferences(),
            authenticator = object : AppLockAuthenticator {
                override fun availability(): AppLockAvailability = AppLockAvailability.Available

                override suspend fun authenticate(): AppLockAuthenticationResult {
                    authenticationStarted.complete(Unit)
                    CompletableDeferred<Unit>().await()
                    error("Authentication should have been cancelled")
                }
            },
        )
        manager.initialize()

        val enabling = async { manager.setAppLockEnabled(true) }
        authenticationStarted.await()
        enabling.cancelAndJoin()
        assertEquals(AppLockStatus.Unlocked, manager.state.value.status)
        assertFalse(manager.state.value.isEnabled)
        assertTrue(manager.state.value.isContentVisible)
        assertEquals(AppLockMessage.AuthenticationCancelled, manager.state.value.message)
    }
    @Test
    fun lockedStateDoesNotExposeProtectedContent() = runTest {
        val preferences = InMemorySensitivePreferences()
        preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, true)
        val manager = appLockManager(preferences = preferences)

        manager.initialize()

        assertFalse(manager.state.value.isContentVisible)
    }

    private fun appLockManager(
        preferences: InMemorySensitivePreferences = InMemorySensitivePreferences(),
        authenticator: FakeAppLockAuthenticator = FakeAppLockAuthenticator(),
    ): AppLockManager = AppLockManager(
        preferences = preferences,
        authenticator = authenticator,
    )

    private class FakeAppLockAuthenticator(
        private val result: AppLockAuthenticationResult = AppLockAuthenticationResult.Success,
        private val availability: AppLockAvailability = AppLockAvailability.Available,
    ) : AppLockAuthenticator {
        override fun availability(): AppLockAvailability = availability

        override suspend fun authenticate(): AppLockAuthenticationResult = result
    }
}
