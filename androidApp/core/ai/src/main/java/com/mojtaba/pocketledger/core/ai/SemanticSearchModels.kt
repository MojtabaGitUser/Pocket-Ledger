package com.mojtaba.pocketledger.core.ai

data class SemanticSearchDocument(
    val id: String,
    val title: String,
    val body: String,
    val metadata: Map<String, String> = emptyMap(),
    val occurredAtMillis: Long? = null,
    val categoryId: String? = null,
    val accountId: String? = null,
)

data class SemanticSearchRequest(
    val query: String,
    val documents: List<SemanticSearchDocument>,
    val maxResults: Int = documents.size,
    val filters: SemanticSearchFilters = SemanticSearchFilters(),
    val privacyMode: AiPrivacyMode = AiPrivacyMode.LocalRawAllowed,
)

data class SemanticSearchResult(
    val rankedIds: List<String>,
    val matches: List<SemanticSearchMatch> = rankedIds.mapIndexed { index, id ->
        SemanticSearchMatch(id = id, relevanceScore = (rankedIds.size - index).coerceAtLeast(1))
    },
)

data class SemanticSearchFilters(
    val categoryIds: Set<String> = emptySet(),
    val accountIds: Set<String> = emptySet(),
)

data class SemanticSearchMatch(
    val id: String,
    val relevanceScore: Int,
    val reason: String? = null,
)
