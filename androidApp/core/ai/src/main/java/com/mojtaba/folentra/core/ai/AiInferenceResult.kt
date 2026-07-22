package com.mojtaba.folentra.core.ai

sealed interface AiInferenceResult<out T> {
    data class Success<T>(
        val value: T,
        val providerType: AiProviderType,
        val fallbackReason: String? = null,
    ) : AiInferenceResult<T>

    data class Unavailable(
        val providerType: AiProviderType,
        val reason: String,
    ) : AiInferenceResult<Nothing>

    data class Failure(
        val providerType: AiProviderType,
        val reason: String,
    ) : AiInferenceResult<Nothing>
}
