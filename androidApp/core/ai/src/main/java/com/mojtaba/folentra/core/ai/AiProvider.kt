package com.mojtaba.folentra.core.ai

interface AiProvider {
    val type: AiProviderType
    val capabilities: AiProviderCapabilities

    fun availability(): AiProviderAvailability
    suspend fun currentAvailability(): AiProviderAvailability = availability()

    suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult>

    suspend fun generateMonthlySummary(
        request: MonthlySummaryRequest,
    ): AiInferenceResult<MonthlySummaryResult>

    suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult>

    suspend fun smartAutofill(request: SmartAutofillRequest): AiInferenceResult<SmartAutofillResult>
}
