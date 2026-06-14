package com.mojtaba.pocketledger.core.security.applock

sealed interface AppLockAuthenticationResult {
    data object Success : AppLockAuthenticationResult
    data object Cancelled : AppLockAuthenticationResult
    data object Failed : AppLockAuthenticationResult
    data class Error(val reason: AppLockUnavailableReason = AppLockUnavailableReason.Unknown) : AppLockAuthenticationResult
}
