package com.mojtaba.pocketledger.core.ai

class MlKitAiProvider(
    private val supported: Boolean = false,
) : AiProvider {
    override val type: AiProviderType = AiProviderType.MlKit
    override val capabilities: AiProviderCapabilities = AiProviderCapabilities(
        monthlySummaries = false,
        semanticSearch = true,
    )

    override fun availability(): AiProviderAvailability =
        if (supported) {
            AiProviderAvailability.Available
        } else {
            AiProviderAvailability.Unavailable("ML Kit AI SDK is not available in this build.")
        }

    override suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult> =
        AiInferenceResult.Unavailable(type, "ML Kit summary generation is not supported.")

    override suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> =
        AiInferenceResult.Unavailable(type, "ML Kit provider is a compile-safe stub.")
}
