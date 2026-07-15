package com.mojtaba.pocketledger.account

import com.mojtaba.pocketledger.core.featureflags.DefaultFeatureFlags
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.pocketledger.core.featureflags.LocalFeatureFlagProvider
import com.mojtaba.pocketledger.core.security.integrity.NoOpPlayIntegrityRequestHook
import com.mojtaba.pocketledger.core.security.integrity.PlayIntegrityAvailability
import com.mojtaba.pocketledger.core.security.integrity.PlayIntegrityRequest
import com.mojtaba.pocketledger.core.security.integrity.PlayIntegrityRequestHook
import com.mojtaba.pocketledger.core.security.integrity.PlayIntegrityTokenResult
import com.mojtaba.pocketledger.core.security.passkey.NoOpPasskeyClient
import com.mojtaba.pocketledger.core.security.passkey.PasskeyAuthenticationOptions
import com.mojtaba.pocketledger.core.security.passkey.PasskeyClient
import com.mojtaba.pocketledger.core.security.passkey.PasskeyClientAvailability
import com.mojtaba.pocketledger.core.security.passkey.PasskeyClientResult
import com.mojtaba.pocketledger.core.security.passkey.PasskeyRegistrationOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OptionalAccountSettingsStateTest {
    @Test
    fun disabledFeatureKeepsAccountEntryLocalOnly() {
        val state = OptionalAccountSettingsState.from(
            featureFlags = FeatureFlagEvaluator(LocalFeatureFlagProvider()),
            passkeyClient = availablePasskeyClient(),
            playIntegrityRequestHook = availableIntegrityHook(),
        )

        assertFalse(state.featureEnabled)
        assertFalse(state.controlsEnabled)
        assertEquals("Off", state.stateDescription)
    }

    @Test
    fun enabledFeatureReportsReadyWhenPasskeyAndIntegrityAreAvailable() {
        val state = OptionalAccountSettingsState(
            featureEnabled = true,
            passkeyClientAvailable = true,
            playIntegrityHookAvailable = true,
        )

        assertTrue(state.controlsEnabled)
        assertEquals("Ready", state.stateDescription)
    }

    @Test
    fun unavailablePasskeyClientDisablesControlsEvenWhenFeatureIsEnabled() {
        val state = OptionalAccountSettingsState(
            featureEnabled = true,
            passkeyClientAvailable = false,
            playIntegrityHookAvailable = true,
        )

        assertFalse(state.controlsEnabled)
        assertEquals("Unavailable", state.stateDescription)
    }

    private fun availablePasskeyClient(): PasskeyClient = object : PasskeyClient {
        override fun availability(): PasskeyClientAvailability = PasskeyClientAvailability.Available
        override suspend fun register(options: PasskeyRegistrationOptions): PasskeyClientResult = NoOpPasskeyClient().register(options)
        override suspend fun authenticate(options: PasskeyAuthenticationOptions): PasskeyClientResult = NoOpPasskeyClient().authenticate(options)
    }

    private fun availableIntegrityHook(): PlayIntegrityRequestHook = object : PlayIntegrityRequestHook {
        override fun availability(): PlayIntegrityAvailability = PlayIntegrityAvailability.Available
        override suspend fun requestToken(request: PlayIntegrityRequest): PlayIntegrityTokenResult =
            PlayIntegrityTokenResult.Success(token = "token")
    }
}