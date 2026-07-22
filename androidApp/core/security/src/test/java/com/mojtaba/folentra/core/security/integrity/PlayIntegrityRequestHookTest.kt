package com.mojtaba.folentra.core.security.integrity

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayIntegrityRequestHookTest {
    @Test
    fun noOpHookReportsDisabledAndNeverRequestsAToken() = runTest {
        val hook = NoOpPlayIntegrityRequestHook()

        assertFalse(hook.availability().available)
        assertEquals(PlayIntegrityUnavailableReason.FeatureDisabled, hook.availability().reason)

        val result = hook.requestToken(PlayIntegrityRequest(nonce = "nonce"))

        assertEquals(PlayIntegrityTokenResult.Unavailable(PlayIntegrityUnavailableReason.FeatureDisabled), result)
    }

    @Test
    fun requestValidatesNonceAndCloudProjectNumber() {
        assertFails { PlayIntegrityRequest(nonce = "") }
        assertFails { PlayIntegrityRequest(nonce = "nonce", cloudProjectNumber = 0L) }
        assertTrue(PlayIntegrityRequest(nonce = "nonce", cloudProjectNumber = 123L).cloudProjectNumber == 123L)
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