package com.mojtaba.pocketledger.core.security.backup

import com.mojtaba.pocketledger.core.security.preferences.DefaultSensitivePreferenceKeys
import com.mojtaba.pocketledger.core.security.preferences.SensitivePreferences

class BackupReadyProfileManager(
    private val preferences: SensitivePreferences,
    private val prerequisitesProvider: suspend (SensitivePreferences) -> BackupReadyProfilePrerequisites = { storedPreferences ->
        BackupReadyProfilePrerequisites(
            passkeyCredentialStored = !storedPreferences.getString(DefaultSensitivePreferenceKeys.PasskeyCredentialId).isNullOrBlank(),
            accountSessionStored = !storedPreferences.getString(DefaultSensitivePreferenceKeys.AccountSessionToken).isNullOrBlank(),
        )
    },
    private val clockMillis: () -> Long = { System.currentTimeMillis() },
) {
    suspend fun state(): BackupReadyProfileState {
        val optInAccepted = preferences.getBoolean(DefaultSensitivePreferenceKeys.BackupReadyProfileOptInAccepted)
        val acceptedAtMillis = preferences.getLong(DefaultSensitivePreferenceKeys.BackupReadyProfileAcceptedAt)
        val policyVersion = preferences.getString(DefaultSensitivePreferenceKeys.BackupReadyProfilePolicyVersion)
        val prerequisites = prerequisitesProvider(preferences)

        return BackupReadyProfileState.from(
            optInAccepted = optInAccepted,
            acceptedAtMillis = acceptedAtMillis,
            policyVersion = policyVersion,
            prerequisites = prerequisites,
        )
    }

    suspend fun setOptInAccepted(accepted: Boolean): BackupReadyProfileState {
        if (accepted) {
            preferences.putBoolean(DefaultSensitivePreferenceKeys.BackupReadyProfileOptInAccepted, true)
            preferences.putLong(DefaultSensitivePreferenceKeys.BackupReadyProfileAcceptedAt, clockMillis())
            preferences.putString(DefaultSensitivePreferenceKeys.BackupReadyProfilePolicyVersion, BackupReadyProfileState.PolicyVersion)
        } else {
            preferences.putBoolean(DefaultSensitivePreferenceKeys.BackupReadyProfileOptInAccepted, false)
            preferences.remove(DefaultSensitivePreferenceKeys.BackupReadyProfileAcceptedAt)
            preferences.remove(DefaultSensitivePreferenceKeys.BackupReadyProfilePolicyVersion)
        }
        return state()
    }
}

data class BackupReadyProfilePrerequisites(
    val passkeyAccountFlowEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = false,
    val passkeyCredentialStored: Boolean = false,
    val accountSessionStored: Boolean = false,
) {
    val accountIdentityReady: Boolean
        get() = passkeyAccountFlowEnabled && passkeyCredentialStored && accountSessionStored

    val encryptedBackupPipelineReady: Boolean
        get() = accountIdentityReady && cloudSyncEnabled
}

data class BackupReadyProfileState(
    val status: BackupReadyProfileStatus,
    val optInAccepted: Boolean,
    val acceptedAtMillis: Long?,
    val policyVersion: String?,
    val prerequisites: BackupReadyProfilePrerequisites,
    val androidBackupIncludesLedgerData: Boolean = false,
) {
    val controlsEnabled: Boolean
        get() = prerequisites.accountIdentityReady

    val canPrepareEncryptedBackup: Boolean
        get() = status == BackupReadyProfileStatus.ReadyForEncryptedBackupPipeline

    val stateDescription: String
        get() = when (status) {
            BackupReadyProfileStatus.LocalOnly -> "Local only"
            BackupReadyProfileStatus.WaitingForAccountIdentity -> "Account required"
            BackupReadyProfileStatus.WaitingForEncryptedBackupPipeline -> "Backup pending"
            BackupReadyProfileStatus.ReadyForEncryptedBackupPipeline -> "Backup ready"
        }

    val supportingText: String
        get() = when (status) {
            BackupReadyProfileStatus.LocalOnly -> "Backup-ready profile is off; ledger data stays local and excluded from Android backup."
            BackupReadyProfileStatus.WaitingForAccountIdentity -> "Create a passkey account profile before enabling backup-ready behavior."
            BackupReadyProfileStatus.WaitingForEncryptedBackupPipeline -> "Profile opt-in is saved, but encrypted backup and restore are not enabled for this build."
            BackupReadyProfileStatus.ReadyForEncryptedBackupPipeline -> "Profile prerequisites are ready for a reviewed encrypted backup pipeline."
        }

    companion object {
        const val PolicyVersion = "backup-ready-profile-v1"

        fun from(
            optInAccepted: Boolean,
            acceptedAtMillis: Long?,
            policyVersion: String?,
            prerequisites: BackupReadyProfilePrerequisites,
        ): BackupReadyProfileState {
            // Consent is fail-closed: partial writes and consent captured under
            // another policy version must never enable a future backup path.
            val validOptIn = optInAccepted &&
                acceptedAtMillis != null &&
                policyVersion == PolicyVersion
            val status = when {
                !validOptIn -> BackupReadyProfileStatus.LocalOnly
                !prerequisites.accountIdentityReady -> BackupReadyProfileStatus.WaitingForAccountIdentity
                !prerequisites.encryptedBackupPipelineReady -> BackupReadyProfileStatus.WaitingForEncryptedBackupPipeline
                else -> BackupReadyProfileStatus.ReadyForEncryptedBackupPipeline
            }
            return BackupReadyProfileState(
                status = status,
                optInAccepted = validOptIn,
                acceptedAtMillis = acceptedAtMillis.takeIf { validOptIn },
                policyVersion = policyVersion.takeIf { validOptIn },
                prerequisites = prerequisites,
            )
        }
    }
}

enum class BackupReadyProfileStatus {
    LocalOnly,
    WaitingForAccountIdentity,
    WaitingForEncryptedBackupPipeline,
    ReadyForEncryptedBackupPipeline,
}
