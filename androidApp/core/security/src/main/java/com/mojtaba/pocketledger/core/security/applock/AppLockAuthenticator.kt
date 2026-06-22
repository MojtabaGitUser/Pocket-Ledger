package com.mojtaba.pocketledger.core.security.applock

interface AppLockAuthenticator {
    fun availability(): AppLockAvailability

    suspend fun authenticate(): AppLockAuthenticationResult
}
