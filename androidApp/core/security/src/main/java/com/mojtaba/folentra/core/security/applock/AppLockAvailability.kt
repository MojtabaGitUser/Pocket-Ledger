package com.mojtaba.folentra.core.security.applock

sealed interface AppLockAvailability {
    data object Available : AppLockAvailability
    data class Unavailable(val reason: AppLockUnavailableReason) : AppLockAvailability
}

enum class AppLockUnavailableReason {
    NoHardware,
    NoneEnrolled,
    DeviceCredentialUnavailable,
    Unknown,
}
