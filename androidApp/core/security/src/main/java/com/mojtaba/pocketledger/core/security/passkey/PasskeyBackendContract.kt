package com.mojtaba.pocketledger.core.security.passkey

sealed interface PasskeyBackendResult<out T> {
    data class Success<T>(val value: T) : PasskeyBackendResult<T>

    data class Unavailable(
        val reason: PasskeyUnavailableReason,
    ) : PasskeyBackendResult<Nothing>

    data class Failure(
        val safeMessage: String,
    ) : PasskeyBackendResult<Nothing> {
        init {
            require(safeMessage.isNotBlank()) { "A safe backend failure message must not be blank." }
        }
    }
}

data class PasskeyRegistrationVerification(
    val profileId: String,
    val credentialId: String,
) {
    init {
        require(profileId.isNotBlank()) { "profileId must not be blank." }
        require(credentialId.isNotBlank()) { "credentialId must not be blank." }
    }
}

data class PasskeyAuthenticationVerification(
    val profileId: String,
    val sessionToken: String,
) {
    init {
        require(profileId.isNotBlank()) { "profileId must not be blank." }
        require(sessionToken.isNotBlank()) { "sessionToken must not be blank." }
    }
}

interface PasskeyBackendContract {
    suspend fun beginRegistration(profile: PasskeyProfile): PasskeyBackendResult<PasskeyRegistrationOptions>

    suspend fun completeRegistration(
        challenge: PasskeyChallenge,
        credentialResponse: PasskeyCredentialResponse,
    ): PasskeyBackendResult<PasskeyRegistrationVerification>

    suspend fun beginAuthentication(username: String? = null): PasskeyBackendResult<PasskeyAuthenticationOptions>

    suspend fun completeAuthentication(
        challenge: PasskeyChallenge,
        credentialResponse: PasskeyCredentialResponse,
    ): PasskeyBackendResult<PasskeyAuthenticationVerification>
}

class NoOpPasskeyBackendContract(
    private val reason: PasskeyUnavailableReason = PasskeyUnavailableReason.BackendUnavailable,
) : PasskeyBackendContract {
    override suspend fun beginRegistration(profile: PasskeyProfile): PasskeyBackendResult<PasskeyRegistrationOptions> =
        PasskeyBackendResult.Unavailable(reason)

    override suspend fun completeRegistration(
        challenge: PasskeyChallenge,
        credentialResponse: PasskeyCredentialResponse,
    ): PasskeyBackendResult<PasskeyRegistrationVerification> = PasskeyBackendResult.Unavailable(reason)

    override suspend fun beginAuthentication(username: String?): PasskeyBackendResult<PasskeyAuthenticationOptions> =
        PasskeyBackendResult.Unavailable(reason)

    override suspend fun completeAuthentication(
        challenge: PasskeyChallenge,
        credentialResponse: PasskeyCredentialResponse,
    ): PasskeyBackendResult<PasskeyAuthenticationVerification> = PasskeyBackendResult.Unavailable(reason)
}