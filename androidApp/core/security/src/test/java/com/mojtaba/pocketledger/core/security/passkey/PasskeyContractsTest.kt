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
    fun noOpBackendKeepsEveryChallengeResponseStepUnavailableUntilARealBackendExists() = runTest {
        val backend = NoOpPasskeyBackendContract()
        val challenge = PasskeyChallenge("challenge")
        val response = PasskeyCredentialResponse("{}")

        val registration = backend.beginRegistration(
            PasskeyProfile(
                profileId = "local-profile",
                username = "local@example.invalid",
                displayName = "Local Profile",
            ),
        )
        val registrationCompletion = backend.completeRegistration(challenge, response)
        val authentication = backend.beginAuthentication("local@example.invalid")
        val authenticationCompletion = backend.completeAuthentication(challenge, response)

        val unavailable = PasskeyBackendResult.Unavailable(PasskeyUnavailableReason.BackendUnavailable)
        assertEquals(unavailable, registration)
        assertEquals(unavailable, registrationCompletion)
        assertEquals(unavailable, authentication)
        assertEquals(unavailable, authenticationCompletion)
    }

    @Test
    fun valueObjectsRejectBlankSecurityInputs() {
        assertFails { PasskeyChallenge(" ") }
        assertFails { PasskeyCredentialResponse("") }
        assertFails { PasskeyBackendResult.Failure(" ") }
        assertFails { PasskeyClientAvailability(available = true, reason = PasskeyUnavailableReason.Unknown) }
        assertFails { PasskeyClientAvailability(available = false) }
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