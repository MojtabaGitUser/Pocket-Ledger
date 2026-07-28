package com.mojtaba.folentra.core.ai

import kotlinx.coroutines.CancellationException

class AiFallbackStrategy(
    private val selector: AiProviderSelector,
    private val ruleBasedProvider: AiProvider = RuleBasedAiProvider,
) {
    suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult> {
        val selected = selector.selectFor(AiCapability.MonthlySummary)
        return selected.executeWithFallback(
            capability = AiCapability.MonthlySummary,
            fallback = { fallbackReason -> ruleBasedProvider.generateSummary(request).withFallbackReason(fallbackReason) },
            operation = { generateSummary(request) },
        )
    }

    suspend fun generateMonthlySummary(
        request: MonthlySummaryRequest,
    ): AiInferenceResult<MonthlySummaryResult> {
        val selected = selector.selectFor(AiCapability.MonthlySummary)
        return selected.executeWithFallback(
            capability = AiCapability.MonthlySummary,
            fallback = { fallbackReason -> ruleBasedProvider.generateMonthlySummary(request).withFallbackReason(fallbackReason) },
            operation = { generateMonthlySummary(request) },
        )
    }

    suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> {
        val selected = selector.selectFor(AiCapability.SemanticSearch)
        return selected.executeWithFallback(
            capability = AiCapability.SemanticSearch,
            fallback = { fallbackReason -> ruleBasedProvider.semanticSearch(request).withFallbackReason(fallbackReason) },
            operation = { semanticSearch(request) },
        )
    }

    suspend fun smartAutofill(request: SmartAutofillRequest): AiInferenceResult<SmartAutofillResult> {
        val selected = selector.selectFor(AiCapability.SmartAutofill)
        return selected.executeWithFallback(
            capability = AiCapability.SmartAutofill,
            fallback = { fallbackReason -> ruleBasedProvider.smartAutofill(request).withFallbackReason(fallbackReason) },
            operation = { smartAutofill(request) },
        )
    }

    private suspend fun <T> AiProvider.executeWithFallback(
        capability: AiCapability,
        fallback: suspend (String) -> AiInferenceResult<T>,
        operation: suspend AiProvider.() -> AiInferenceResult<T>,
    ): AiInferenceResult<T> {
        if (type == AiProviderType.NoOp) {
            return operation()
        }
        if (safeCurrentAvailability() !is AiProviderAvailability.Available || !capabilities.supports(capability)) {
            return fallback("${type.name} is unavailable.")
        }
        return try {
            when (val result = operation()) {
                is AiInferenceResult.Success -> result
                is AiInferenceResult.Unavailable -> fallback(result.reason)
                is AiInferenceResult.Failure -> fallback(result.reason)
            }
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            fallback(throwable.message ?: "${type.name} failed.")
        }
    }

    private suspend fun AiProvider.safeCurrentAvailability(): AiProviderAvailability =
        try {
            currentAvailability()
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            AiProviderAvailability.Unavailable(throwable.message ?: "${type.name} availability check failed.")
        }

    private fun <T> AiInferenceResult<T>.withFallbackReason(reason: String): AiInferenceResult<T> =
        when (this) {
            is AiInferenceResult.Success -> copy(fallbackReason = reason)
            is AiInferenceResult.Unavailable -> this
            is AiInferenceResult.Failure -> this
        }
}
