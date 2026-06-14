package com.mojtaba.pocketledger.core.ai

class GeminiNanoAiProvider(
    private val supported: Boolean = false,
) : AiProvider {
    override val type: AiProviderType = AiProviderType.GeminiNano
    override val capabilities: AiProviderCapabilities = AiProviderCapabilities.SummariesAndSearch

    override fun availability(): AiProviderAvailability =
        if (supported) {
            AiProviderAvailability.Available
        } else {
            AiProviderAvailability.Unavailable("Gemini Nano SDK is not available in this build.")
        }

    override suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult> =
        AiInferenceResult.Unavailable(type, "Gemini Nano provider is a compile-safe stub.")

    override suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> =
        AiInferenceResult.Unavailable(type, "Gemini Nano provider is a compile-safe stub.")
}
