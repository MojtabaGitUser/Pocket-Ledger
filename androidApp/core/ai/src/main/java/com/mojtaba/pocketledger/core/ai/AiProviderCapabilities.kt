package com.mojtaba.pocketledger.core.ai

data class AiProviderCapabilities(
    val monthlySummaries: Boolean,
    val semanticSearch: Boolean,
    val smartAutofill: Boolean = false,
) {
    fun supports(capability: AiCapability): Boolean =
        when (capability) {
            AiCapability.MonthlySummary -> monthlySummaries
            AiCapability.SemanticSearch -> semanticSearch
            AiCapability.SmartAutofill -> smartAutofill
        }

    companion object {
        val None = AiProviderCapabilities(
            monthlySummaries = false,
            semanticSearch = false,
            smartAutofill = false,
        )

        val SummariesAndSearch = AiProviderCapabilities(
            monthlySummaries = true,
            semanticSearch = true,
            smartAutofill = false,
        )

        val LocalFinanceFeatures = AiProviderCapabilities(
            monthlySummaries = true,
            semanticSearch = true,
            smartAutofill = true,
        )
    }
}
