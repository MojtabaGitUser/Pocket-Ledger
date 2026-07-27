package com.mojtaba.folentra.core.security.backup

import com.mojtaba.folentra.core.security.preferences.DefaultSensitivePreferenceKeys
import com.mojtaba.folentra.core.security.preferences.InMemorySensitivePreferences
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupReadyProfileManagerTest {
    @Test
    fun defaultStateIsLocalOnlyAndDoesNotIncludeLedgerInAndroidBackup() = runTest {
        val manager = BackupReadyProfileManager(preferences = InMemorySensitivePreferences())

        val state = manager.state()

        assertEquals(BackupReadyProfileStatus.LocalOnly, state.status)
        assertFalse(state.optInAccepted)
        assertFalse(state.controlsEnabled)
        assertFalse(state.androidBackupIncludesLedgerData)
        assertEquals("Local only", state.stateDescription)
    }

    @Test
    fun acceptingOptInWithoutAccountIdentityWaitsForAccountPrerequisites() = runTest {
        val preferences = InMemorySensitivePreferences()
        val manager = BackupReadyProfileManager(
            preferences = preferences,
            prerequisitesProvider = {
                BackupReadyProfilePrerequisites(
                    passkeyAccountFlowEnabled = true,
                    cloudSyncEnabled = false,
                    passkeyCredentialStored = false,
                    accountSessionStored = false,
                )
            },
            clockMillis = { 42L },
        )

        val state = manager.setOptInAccepted(true)

        assertEquals(BackupReadyProfileStatus.WaitingForAccountIdentity, state.status)
        assertTrue(state.optInAccepted)
        assertEquals(42L, state.acceptedAtMillis)
        assertEquals(BackupReadyProfileState.PolicyVersion, state.policyVersion)
        assertFalse(state.androidBackupIncludesLedgerData)
    }

    @Test
    fun acceptingOptInWithAccountButWithoutCloudSyncWaitsForEncryptedBackupPipeline() = runTest {
        val manager = BackupReadyProfileManager(
            preferences = InMemorySensitivePreferences(),
            prerequisitesProvider = {
                BackupReadyProfilePrerequisites(
                    passkeyAccountFlowEnabled = true,
                    cloudSyncEnabled = false,
                    passkeyCredentialStored = true,
                    accountSessionStored = true,
                )
            },
        )

        val state = manager.setOptInAccepted(true)

        assertEquals(BackupReadyProfileStatus.WaitingForEncryptedBackupPipeline, state.status)
        assertTrue(state.controlsEnabled)
        assertFalse(state.canPrepareEncryptedBackup)
        assertFalse(state.androidBackupIncludesLedgerData)
    }

    @Test
    fun acceptingOptInWithAllPrerequisitesReportsReadyForEncryptedBackupPipeline() = runTest {
        val manager = BackupReadyProfileManager(
            preferences = InMemorySensitivePreferences(),
            prerequisitesProvider = {
                BackupReadyProfilePrerequisites(
                    passkeyAccountFlowEnabled = true,
                    cloudSyncEnabled = true,
                    passkeyCredentialStored = true,
                    accountSessionStored = true,
                )
            },
        )

        val state = manager.setOptInAccepted(true)

        assertEquals(BackupReadyProfileStatus.ReadyForEncryptedBackupPipeline, state.status)
        assertTrue(state.canPrepareEncryptedBackup)
        assertFalse(state.androidBackupIncludesLedgerData)
    }

    @Test
    fun clearingOptInRemovesAcceptedAtAndPolicyVersion() = runTest {
        val preferences = InMemorySensitivePreferences()
        val manager = BackupReadyProfileManager(
            preferences = preferences,
            prerequisitesProvider = {
                BackupReadyProfilePrerequisites(
                    passkeyAccountFlowEnabled = true,
                    cloudSyncEnabled = true,
                    passkeyCredentialStored = true,
                    accountSessionStored = true,
                )
            },
            clockMillis = { 42L },
        )
        manager.setOptInAccepted(true)

        val state = manager.setOptInAccepted(false)

        assertEquals(BackupReadyProfileStatus.LocalOnly, state.status)
        assertFalse(preferences.getBoolean(DefaultSensitivePreferenceKeys.BackupReadyProfileOptInAccepted))
        assertNull(preferences.getLong(DefaultSensitivePreferenceKeys.BackupReadyProfileAcceptedAt))
        assertNull(preferences.getString(DefaultSensitivePreferenceKeys.BackupReadyProfilePolicyVersion))
    }

    @Test
    fun incompleteOrOutdatedConsentMetadataFailsClosed() {
        val prerequisites = BackupReadyProfilePrerequisites(
            passkeyAccountFlowEnabled = true,
            cloudSyncEnabled = true,
            passkeyCredentialStored = true,
            accountSessionStored = true,
        )

        val missingTimestamp = BackupReadyProfileState.from(
            optInAccepted = true,
            acceptedAtMillis = null,
            policyVersion = BackupReadyProfileState.PolicyVersion,
            prerequisites = prerequisites,
        )
        val outdatedPolicy = BackupReadyProfileState.from(
            optInAccepted = true,
            acceptedAtMillis = 42L,
            policyVersion = "backup-ready-profile-v0",
            prerequisites = prerequisites,
        )

        listOf(missingTimestamp, outdatedPolicy).forEach { state ->
            assertEquals(BackupReadyProfileStatus.LocalOnly, state.status)
            assertFalse(state.optInAccepted)
            assertNull(state.acceptedAtMillis)
            assertNull(state.policyVersion)
            assertFalse(state.canPrepareEncryptedBackup)
        }
    }
}
