package com.mojtaba.pocketledger.account

import com.mojtaba.pocketledger.core.featureflags.DefaultFeatureFlags
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.pocketledger.core.security.integrity.PlayIntegrityRequestHook
import com.mojtaba.pocketledger.core.security.passkey.PasskeyClient

data class OptionalAccountSettingsState(
    val featureEnabled: Boolean,
    val passkeyClientAvailable: Boolean,
    val playIntegrityHookAvailable: Boolean,
) {
    val stateDescription: String
        get() = when {
            !featureEnabled -> "Off"
            passkeyClientAvailable && playIntegrityHookAvailable -> "Ready"
            passkeyClientAvailable -> "Partially ready"
            else -> "Unavailable"
        }

    val controlsEnabled: Boolean
        get() = featureEnabled && passkeyClientAvailable

    val supportingText: String
        get() = when {
            !featureEnabled -> "Optional account and passkey profile is off by default; Pocket Ledger stays fully local without login."
            passkeyClientAvailable && playIntegrityHookAvailable -> "Prototype passkey client and Play Integrity request hook are available for a future opt-in profile."
            passkeyClientAvailable -> "Prototype passkey client is available; Play Integrity enforcement is not configured."
            else -> "This device or build cannot start a passkey profile right now."
        }

    companion object {
        fun from(
            featureFlags: FeatureFlagEvaluator,
            passkeyClient: PasskeyClient,
            playIntegrityRequestHook: PlayIntegrityRequestHook,
        ): OptionalAccountSettingsState = OptionalAccountSettingsState(
            featureEnabled = featureFlags.isEnabled(DefaultFeatureFlags.PasskeyAccountFlowEnabled),
            passkeyClientAvailable = passkeyClient.availability().available,
            playIntegrityHookAvailable = playIntegrityRequestHook.availability().available,
        )
    }
}