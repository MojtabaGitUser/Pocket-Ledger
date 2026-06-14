package com.mojtaba.pocketledger.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mojtaba.pocketledger.core.security.applock.AppLockAuthenticationResult
import com.mojtaba.pocketledger.core.security.applock.AppLockAuthenticator
import com.mojtaba.pocketledger.core.security.applock.AppLockAvailability
import com.mojtaba.pocketledger.core.security.applock.AppLockUnavailableReason
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidBiometricAppLockAuthenticator(
    private val biometricManager: BiometricManager,
    private val activityProvider: () -> FragmentActivity?,
) : AppLockAuthenticator {
    override fun availability(): AppLockAvailability =
        when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> AppLockAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> unavailable(AppLockUnavailableReason.NoHardware)
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> unavailable(AppLockUnavailableReason.NoHardware)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> unavailable(AppLockUnavailableReason.NoneEnrolled)
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> unavailable(AppLockUnavailableReason.Unknown)
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> unavailable(AppLockUnavailableReason.DeviceCredentialUnavailable)
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> unavailable(AppLockUnavailableReason.Unknown)
            else -> unavailable(AppLockUnavailableReason.Unknown)
        }

    override suspend fun authenticate(): AppLockAuthenticationResult {
        val activity = activityProvider() ?: return AppLockAuthenticationResult.Error()
        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) {
                            continuation.resume(AppLockAuthenticationResult.Success)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (continuation.isActive) {
                            continuation.resume(errorCode.toResult())
                        }
                    }
                },
            )

            continuation.invokeOnCancellation {
                prompt.cancelAuthentication()
            }

            prompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Pocket Ledger")
                    .setSubtitle("Confirm it is you to view your ledger.")
                    .setAllowedAuthenticators(AUTHENTICATORS)
                    .build(),
            )
        }
    }

    private fun unavailable(reason: AppLockUnavailableReason): AppLockAvailability.Unavailable =
        AppLockAvailability.Unavailable(reason)

    private fun Int.toResult(): AppLockAuthenticationResult =
        when (this) {
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_USER_CANCELED,
            -> AppLockAuthenticationResult.Cancelled
            BiometricPrompt.ERROR_NO_BIOMETRICS -> AppLockAuthenticationResult.Error(AppLockUnavailableReason.NoneEnrolled)
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> AppLockAuthenticationResult.Error(AppLockUnavailableReason.DeviceCredentialUnavailable)
            BiometricPrompt.ERROR_HW_NOT_PRESENT -> AppLockAuthenticationResult.Error(AppLockUnavailableReason.NoHardware)
            else -> AppLockAuthenticationResult.Error(AppLockUnavailableReason.Unknown)
        }

    companion object {
        private const val AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    }
}
