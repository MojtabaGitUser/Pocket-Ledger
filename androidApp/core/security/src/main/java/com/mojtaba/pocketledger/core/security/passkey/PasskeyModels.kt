package com.mojtaba.pocketledger.core.security.passkey

@JvmInline
value class PasskeyChallenge(val value: String) {
    init {
        require(value.isNotBlank()) { "Passkey challenge must not be blank." }
    }
}

data class PasskeyProfile(
    val profileId: String,
    val username: String,
    val displayName: String,
) {
    init {
        require(profileId.isNotBlank()) { "profileId must not be blank." }
        require(username.isNotBlank()) { "username must not be blank." }
        require(displayName.isNotBlank()) { "displayName must not be blank." }
    }
}

data class PasskeyRegistrationOptions(
    val challenge: PasskeyChallenge,
    val requestJson: String,
) {
    init {
        require(requestJson.isNotBlank()) { "Credential Manager registration request JSON must not be blank." }
    }
}

data class PasskeyAuthenticationOptions(
    val challenge: PasskeyChallenge,
    val requestJson: String,
) {
    init {
        require(requestJson.isNotBlank()) { "Credential Manager authentication request JSON must not be blank." }
    }
}

data class PasskeyCredentialResponse(
    val responseJson: String,
) {
    init {
        require(responseJson.isNotBlank()) { "Credential response JSON must not be blank." }
    }
}

data class PasskeyClientAvailability(
    val available: Boolean,
    val reason: PasskeyUnavailableReason? = null,
) {
    init {
        require(available == (reason == null)) {
            "Available passkey clients must not include a reason; unavailable clients must include one."
        }
    }

    companion object {
        val Available = PasskeyClientAvailability(available = true)
    }
}

enum class PasskeyUnavailableReason {
    FeatureDisabled,
    PlatformUnsupported,
    CredentialProviderUnavailable,
    BackendUnavailable,
    PlayIntegrityUnavailable,
    Unknown,
}

enum class PasskeyClientError {
    Cancelled,
    InvalidRequest,
    ProviderFailure,
    Unsupported,
    Unknown,
}

sealed interface PasskeyClientResult {
    data class Success(
        val credentialResponse: PasskeyCredentialResponse,
    ) : PasskeyClientResult

    data class Unavailable(
        val reason: PasskeyUnavailableReason,
    ) : PasskeyClientResult

    data class Failure(
        val error: PasskeyClientError,
        val safeMessage: String? = null,
    ) : PasskeyClientResult
}

interface PasskeyClient {
    fun availability(): PasskeyClientAvailability

    suspend fun register(options: PasskeyRegistrationOptions): PasskeyClientResult

    suspend fun authenticate(options: PasskeyAuthenticationOptions): PasskeyClientResult
}

class NoOpPasskeyClient(
    private val reason: PasskeyUnavailableReason = PasskeyUnavailableReason.FeatureDisabled,
) : PasskeyClient {
    override fun availability(): PasskeyClientAvailability = PasskeyClientAvailability(
        available = false,
        reason = reason,
    )

    override suspend fun register(options: PasskeyRegistrationOptions): PasskeyClientResult =
        PasskeyClientResult.Unavailable(reason)

    override suspend fun authenticate(options: PasskeyAuthenticationOptions): PasskeyClientResult =
        PasskeyClientResult.Unavailable(reason)
}