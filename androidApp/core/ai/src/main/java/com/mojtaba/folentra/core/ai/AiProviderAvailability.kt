package com.mojtaba.folentra.core.ai

sealed interface AiProviderAvailability {
    data object Available : AiProviderAvailability

    data class Unavailable(
        val reason: String,
    ) : AiProviderAvailability
}
