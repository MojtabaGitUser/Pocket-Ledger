package com.mojtaba.folentra.core.security.applock

interface AppLockAuthenticator {
    fun availability(): AppLockAvailability

    suspend fun authenticate(): AppLockAuthenticationResult
}
