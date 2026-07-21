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
    val authenticationUiState: OptionalAccountAuthenticationUiState
        get() = when {
            !featureEnabled -> OptionalAccountAuthenticationUiState.LocalOnly
            passkeyClientAvailable && playIntegrityHookAvailable -> OptionalAccountAuthenticationUiState.Ready
            passkeyClientAvailable -> OptionalAccountAuthenticationUiState.ReadyWithoutIntegrity
            else -> OptionalAccountAuthenticationUiState.ProviderUnavailable
        }

    val stateDescription: String
        get() = when (authenticationUiState) {
            OptionalAccountAuthenticationUiState.LocalOnly -> "Off"
            OptionalAccountAuthenticationUiState.Ready -> "Ready"
            OptionalAccountAuthenticationUiState.ReadyWithoutIntegrity -> "Partially ready"
            OptionalAccountAuthenticationUiState.ProviderUnavailable -> "Unavailable"
        }

    val controlsEnabled: Boolean
        get() = featureEnabled && passkeyClientAvailable

    val supportingText: String
        get() = when (authenticationUiState) {
            OptionalAccountAuthenticationUiState.LocalOnly -> "Optional account and passkey profile is off by default; Pocket Ledger stays fully local without login."
            OptionalAccountAuthenticationUiState.Ready -> "Prototype passkey client and Play Integrity request hook are available for a future opt-in profile."
            OptionalAccountAuthenticationUiState.ReadyWithoutIntegrity -> "Prototype passkey client is available; Play Integrity enforcement is not configured."
            OptionalAccountAuthenticationUiState.ProviderUnavailable -> "This device or build cannot start a passkey profile right now."
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

enum class OptionalAccountAuthenticationUiState {
    LocalOnly,
    Ready,
    ReadyWithoutIntegrity,
    ProviderUnavailable,
}
