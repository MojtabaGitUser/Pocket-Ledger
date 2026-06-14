package com.mojtaba.pocketledger.core.ai

data class AiProviderCapabilities(
    val monthlySummaries: Boolean,
    val semanticSearch: Boolean,
) {
    fun supports(capability: AiCapability): Boolean =
        when (capability) {
            AiCapability.MonthlySummary -> monthlySummaries
            AiCapability.SemanticSearch -> semanticSearch
        }

    companion object {
        val None = AiProviderCapabilities(
            monthlySummaries = false,
            semanticSearch = false,
        )

        val SummariesAndSearch = AiProviderCapabilities(
            monthlySummaries = true,
            semanticSearch = true,
        )
    }
}
