package com.mojtaba.pocketledger.core.security.passkey

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasskeyContractsTest {
    @Test
    fun noOpPasskeyClientReportsDisabledAndDoesNotStartCredentialFlow() = runTest {
        val client = NoOpPasskeyClient()

        assertFalse(client.availability().available)
        assertEquals(PasskeyUnavailableReason.FeatureDisabled, client.availability().reason)

        val result = client.authenticate(
            PasskeyAuthenticationOptions(
                challenge = PasskeyChallenge("challenge"),
                requestJson = "{}",
            ),
        )

        assertEquals(PasskeyClientResult.Unavailable(PasskeyUnavailableReason.FeatureDisabled), result)
    }

    @Test
    fun noOpBackendKeepsAccountFlowUnavailableUntilARealBackendExists() = runTest {
        val backend = NoOpPasskeyBackendContract()

        val result = backend.beginRegistration(
            PasskeyProfile(
                profileId = "local-profile",
                username = "local@example.invalid",
                displayName = "Local Profile",
            ),
        )

        assertEquals(PasskeyBackendResult.Unavailable(PasskeyUnavailableReason.BackendUnavailable), result)
    }

    @Test
    fun valueObjectsRejectBlankSecurityInputs() {
        assertFails { PasskeyChallenge(" ") }
        assertFails { PasskeyCredentialResponse("") }
        assertTrue(PasskeyClientAvailability.Available.available)
    }

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        }
        assertTrue("Expected IllegalArgumentException", failed)
    }
}