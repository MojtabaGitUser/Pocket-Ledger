package com.mojtaba.pocketledger.core.ai

data class SemanticSearchDocument(
    val id: String,
    val title: String,
    val body: String,
    val metadata: Map<String, String> = emptyMap(),
)

data class SemanticSearchRequest(
    val query: String,
    val documents: List<SemanticSearchDocument>,
    val maxResults: Int = documents.size,
)

data class SemanticSearchResult(
    val rankedIds: List<String>,
)
