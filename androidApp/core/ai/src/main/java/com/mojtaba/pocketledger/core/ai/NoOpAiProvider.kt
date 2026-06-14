package com.mojtaba.pocketledger.core.ai

object NoOpAiProvider : AiProvider {
    override val type: AiProviderType = AiProviderType.NoOp
    override val capabilities: AiProviderCapabilities = AiProviderCapabilities.None

    override fun availability(): AiProviderAvailability =
        AiProviderAvailability.Unavailable("On-device AI is disabled.")

    override suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult> =
        AiInferenceResult.Success(
            value = AiSummaryResult(""),
            providerType = type,
        )

    override suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> =
        AiInferenceResult.Success(
            value = SemanticSearchResult(emptyList()),
            providerType = type,
        )
}
