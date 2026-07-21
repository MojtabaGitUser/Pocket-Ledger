package com.mojtaba.pocketledger.core.security.preferences

object DefaultSensitivePreferenceKeys {
    val PasskeyCredentialId = StringPreferenceKey("passkey_credential_id")
    val AccountSessionToken = StringPreferenceKey("account_session_token")
    val LastSecurityCheckAt = LongPreferenceKey("last_security_check_at")
    val BiometricUnlockEnabled = BooleanPreferenceKey("biometric_unlock_enabled")
    val BackupReadyProfileOptInAccepted = BooleanPreferenceKey("backup_ready_profile_opt_in_accepted")
    val BackupReadyProfileAcceptedAt = LongPreferenceKey("backup_ready_profile_accepted_at")
    val BackupReadyProfilePolicyVersion = StringPreferenceKey("backup_ready_profile_policy_version")

    val All: List<SensitivePreferenceKey<*>> = listOf(
        PasskeyCredentialId,
        AccountSessionToken,
        LastSecurityCheckAt,
        BiometricUnlockEnabled,
        BackupReadyProfileOptInAccepted,
        BackupReadyProfileAcceptedAt,
        BackupReadyProfilePolicyVersion,
    )
}
