package com.mojtaba.folentra.core.ai

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

    override suspend fun generateMonthlySummary(
        request: MonthlySummaryRequest,
    ): AiInferenceResult<MonthlySummaryResult> =
        AiInferenceResult.Unavailable(type, "AI monthly summaries are disabled.")

    override suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> =
        AiInferenceResult.Success(
            value = SemanticSearchResult(emptyList()),
            providerType = type,
        )

    override suspend fun smartAutofill(request: SmartAutofillRequest): AiInferenceResult<SmartAutofillResult> =
        AiInferenceResult.Unavailable(type, "AI smart autofill is disabled.")
}
