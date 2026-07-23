package com.mojtaba.folentra.core.security.applock

data class AppLockState(
    val status: AppLockStatus = AppLockStatus.Loading,
    val isEnabled: Boolean = false,
    val availability: AppLockAvailability = AppLockAvailability.Unavailable(AppLockUnavailableReason.Unknown),
    val lockedChallengeId: Long = 0L,
    val message: AppLockMessage? = null,
) {
    val canEnable: Boolean
        get() = availability is AppLockAvailability.Available

    val isContentVisible: Boolean
        get() = status == AppLockStatus.Unlocked ||
            (status == AppLockStatus.Authenticating && !isEnabled)
}

enum class AppLockStatus {
    Loading,
    Unlocked,
    Locked,
    Authenticating,
    Unavailable,
}

enum class AppLockMessage {
    AuthenticationCancelled,
    AuthenticationFailed,
    AuthenticationError,
    AppLockUnavailable,
}
