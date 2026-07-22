package com.mojtaba.folentra.core.security.integrity

data class PlayIntegrityRequest(
    val nonce: String,
    val cloudProjectNumber: Long? = null,
) {
    init {
        require(nonce.isNotBlank()) { "Play Integrity nonce must not be blank." }
        require(cloudProjectNumber == null || cloudProjectNumber > 0L) {
            "cloudProjectNumber must be positive when provided."
        }
    }
}

data class PlayIntegrityAvailability(
    val available: Boolean,
    val reason: PlayIntegrityUnavailableReason? = null,
) {
    init {
        require(available || reason != null) { "Unavailable Play Integrity hooks must include a reason." }
    }

    companion object {
        val Available = PlayIntegrityAvailability(available = true)
    }
}

enum class PlayIntegrityUnavailableReason {
    FeatureDisabled,
    PlayServicesUnavailable,
    NetworkRequired,
    NotConfigured,
    Unknown,
}

enum class PlayIntegrityError {
    InvalidRequest,
    ProviderFailure,
    Unknown,
}

sealed interface PlayIntegrityTokenResult {
    data class Success(
        val token: String,
    ) : PlayIntegrityTokenResult

    data class Unavailable(
        val reason: PlayIntegrityUnavailableReason,
    ) : PlayIntegrityTokenResult

    data class Failure(
        val error: PlayIntegrityError,
        val safeMessage: String? = null,
    ) : PlayIntegrityTokenResult
}

interface PlayIntegrityRequestHook {
    fun availability(): PlayIntegrityAvailability

    suspend fun requestToken(request: PlayIntegrityRequest): PlayIntegrityTokenResult
}

class NoOpPlayIntegrityRequestHook(
    private val reason: PlayIntegrityUnavailableReason = PlayIntegrityUnavailableReason.FeatureDisabled,
) : PlayIntegrityRequestHook {
    override fun availability(): PlayIntegrityAvailability = PlayIntegrityAvailability(
        available = false,
        reason = reason,
    )

    override suspend fun requestToken(request: PlayIntegrityRequest): PlayIntegrityTokenResult =
        PlayIntegrityTokenResult.Unavailable(reason)
}