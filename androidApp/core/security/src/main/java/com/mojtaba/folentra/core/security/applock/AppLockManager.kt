package com.mojtaba.folentra.core.security.applock

import com.mojtaba.folentra.core.security.preferences.DefaultSensitivePreferenceKeys
import com.mojtaba.folentra.core.security.preferences.SensitivePreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppLockManager(
    private val preferences: SensitivePreferences,
    private val authenticator: AppLockAuthenticator,
) {
    private val mutex = Mutex()
    private var initialized = false
    private var nextChallengeId = 1L
    private val mutableState = MutableStateFlow(AppLockState())

    val state: StateFlow<AppLockState> = mutableState.asStateFlow()

    suspend fun initialize() {
        mutex.withLock {
            if (initialized) return

            val availability = authenticator.availability()
            val storedEnabled = preferences.getBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled)
            initialized = true

            mutableState.value = when {
                availability !is AppLockAvailability.Available -> {
                    if (storedEnabled) {
                        preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, false)
                    }
                    AppLockState(
                        status = AppLockStatus.Unlocked,
                        isEnabled = false,
                        availability = availability,
                        message = AppLockMessage.AppLockUnavailable,
                    )
                }
                storedEnabled -> lockedState(availability)
                else -> AppLockState(
                    status = AppLockStatus.Unlocked,
                    isEnabled = false,
                    availability = availability,
                )
            }
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean): Boolean {
        val shouldAuthenticate = mutex.withLock {
            ensureInitialized()
            val current = mutableState.value
            val availability = authenticator.availability()

            if (!enabled) {
                preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, false)
                mutableState.value = current.copy(
                    status = AppLockStatus.Unlocked,
                    isEnabled = false,
                    availability = availability,
                    message = null,
                )
                return true
            }

            if (availability !is AppLockAvailability.Available) {
                preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, false)
                mutableState.value = AppLockState(
                    status = AppLockStatus.Unlocked,
                    isEnabled = false,
                    availability = availability,
                    message = AppLockMessage.AppLockUnavailable,
                )
                return false
            }

            if (current.status == AppLockStatus.Authenticating) {
                return false
            }

            mutableState.value = current.copy(
                status = AppLockStatus.Authenticating,
                availability = availability,
                message = null,
            )
            true
        }

        if (!shouldAuthenticate) return false

        val result = try {
            authenticator.authenticate()
        } catch (cancellation: CancellationException) {
            withContext(NonCancellable) {
                mutex.withLock {
                    preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, false)
                    mutableState.value = mutableState.value.copy(
                        status = AppLockStatus.Unlocked,
                        isEnabled = false,
                        availability = authenticator.availability(),
                        message = AppLockMessage.AuthenticationCancelled,
                    )
                }
            }
            throw cancellation
        }

        return when (result) {
            AppLockAuthenticationResult.Success -> {
                mutex.withLock {
                    preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, true)
                    mutableState.value = mutableState.value.copy(
                        status = AppLockStatus.Unlocked,
                        isEnabled = true,
                        availability = authenticator.availability(),
                        message = null,
                    )
                }
                true
            }
            else -> {
                mutex.withLock {
                    preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, false)
                    mutableState.value = mutableState.value.copy(
                        status = AppLockStatus.Unlocked,
                        isEnabled = false,
                        availability = authenticator.availability(),
                        message = result.toMessage(),
                    )
                }
                false
            }
        }
    }

    suspend fun unlock() {
        val shouldAuthenticate = mutex.withLock {
            ensureInitialized()
            val current = mutableState.value
            if (!current.isEnabled || current.status == AppLockStatus.Unlocked || current.status == AppLockStatus.Authenticating) {
                return
            }

            val availability = authenticator.availability()
            if (availability !is AppLockAvailability.Available) {
                preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, false)
                mutableState.value = AppLockState(
                    status = AppLockStatus.Unlocked,
                    isEnabled = false,
                    availability = availability,
                    message = AppLockMessage.AppLockUnavailable,
                )
                return
            }

            mutableState.value = current.copy(
                status = AppLockStatus.Authenticating,
                availability = availability,
                message = null,
            )
            true
        }

        if (!shouldAuthenticate) return

        val result = authenticator.authenticate()
        mutex.withLock {
            mutableState.value = when (result) {
                AppLockAuthenticationResult.Success -> mutableState.value.copy(
                    status = AppLockStatus.Unlocked,
                    availability = authenticator.availability(),
                    message = null,
                )
                else -> mutableState.value.copy(
                    status = AppLockStatus.Locked,
                    availability = authenticator.availability(),
                    lockedChallengeId = mutableState.value.lockedChallengeId,
                    message = result.toMessage(),
                )
            }
        }
    }

    suspend fun onAppForegrounded() {
        mutex.withLock {
            if (!initialized) return
            val current = mutableState.value
            if (current.isEnabled && current.status == AppLockStatus.Unlocked) {
                mutableState.value = lockedState(current.availability)
            }
        }
    }

    private suspend fun ensureInitialized() {
        if (!initialized) {
            val availability = authenticator.availability()
            val storedEnabled = preferences.getBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled)
            initialized = true
            mutableState.value = when {
                availability !is AppLockAvailability.Available -> AppLockState(
                    status = AppLockStatus.Unlocked,
                    isEnabled = false,
                    availability = availability,
                    message = AppLockMessage.AppLockUnavailable,
                )
                storedEnabled -> lockedState(availability)
                else -> AppLockState(
                    status = AppLockStatus.Unlocked,
                    isEnabled = false,
                    availability = availability,
                )
            }
        }
    }

    private fun lockedState(availability: AppLockAvailability): AppLockState =
        AppLockState(
            status = AppLockStatus.Locked,
            isEnabled = true,
            availability = availability,
            lockedChallengeId = nextChallengeId++,
        )

    private fun AppLockAuthenticationResult.toMessage(): AppLockMessage =
        when (this) {
            AppLockAuthenticationResult.Success -> error("Successful authentication does not produce an error message.")
            AppLockAuthenticationResult.Cancelled -> AppLockMessage.AuthenticationCancelled
            AppLockAuthenticationResult.Failed -> AppLockMessage.AuthenticationFailed
            is AppLockAuthenticationResult.Error -> AppLockMessage.AuthenticationError
        }
}
