package com.mojtaba.pocketledger.core.ai

import java.util.Locale

object RuleBasedAiProvider : AiProvider {
    override val type: AiProviderType = AiProviderType.RuleBased
    override val capabilities: AiProviderCapabilities = AiProviderCapabilities.SummariesAndSearch

    override fun availability(): AiProviderAvailability = AiProviderAvailability.Available

    override suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult> {
        val facts = request.facts.mapNotNull { it.cleanOrNull() }
        val text = if (facts.isEmpty()) {
            "No local activity was available for ${request.periodLabel}."
        } else {
            facts.take(request.maxSentences.coerceAtLeast(1)).joinToString(separator = " ")
        }
        return AiInferenceResult.Success(
            value = AiSummaryResult(text),
            providerType = type,
        )
    }

    override suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> {
        val queryTokens = request.query.tokens()
        val rankedIds = if (queryTokens.isEmpty()) {
            request.documents.map { it.id }
        } else {
            request.documents
                .map { document -> document to document.score(queryTokens) }
                .filter { (_, score) -> score > 0 }
                .sortedWith(
                    compareByDescending<Pair<SemanticSearchDocument, Int>> { it.second }
                        .thenBy { it.first.title.lowercase(Locale.US) }
                        .thenBy { it.first.id },
                )
                .take(request.maxResults.coerceAtLeast(0))
                .map { (document, _) -> document.id }
        }
        return AiInferenceResult.Success(
            value = SemanticSearchResult(rankedIds),
            providerType = type,
        )
    }

    private fun SemanticSearchDocument.score(queryTokens: Set<String>): Int {
        val haystack = listOf(title, body, metadata.values.joinToString(" ")).joinToString(" ").lowercase(Locale.US)
        return queryTokens.sumOf { token ->
            when {
                haystack.contains(token) -> 2
                token.length >= 4 && haystack.tokens().any { candidate -> candidate.contains(token) } -> 1
                else -> 0
            }
        }
    }

    private fun String?.cleanOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun String.tokens(): Set<String> =
        lowercase(Locale.US)
            .split(Regex("[^a-z0-9]+"))
            .mapNotNull { it.cleanOrNull() }
            .toSet()
}
