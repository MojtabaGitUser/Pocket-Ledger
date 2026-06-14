package com.mojtaba.pocketledger.core.ai

interface AiProvider {
    val type: AiProviderType
    val capabilities: AiProviderCapabilities

    fun availability(): AiProviderAvailability

    suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult>

    suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult>
}
