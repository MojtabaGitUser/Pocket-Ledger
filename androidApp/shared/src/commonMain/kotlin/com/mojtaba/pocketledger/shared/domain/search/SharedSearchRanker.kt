package com.mojtaba.pocketledger.shared.domain.search

data class SharedSearchDocument(
    val id: String,
    val title: String,
    val body: String,
    val metadata: Map<String, String> = emptyMap(),
    val categoryId: String? = null,
    val accountId: String? = null,
)

data class SharedSearchMatch(
    val id: String,
    val relevanceScore: Int,
    val reason: String,
)

object SharedSearchRanker {
    fun rank(
        query: String,
        documents: List<SharedSearchDocument>,
        maxResults: Int,
        categoryIds: Set<String> = emptySet(),
        accountIds: Set<String> = emptySet(),
    ): List<SharedSearchMatch> {
        val filteredDocuments = documents.filter { document ->
            (categoryIds.isEmpty() || document.categoryId in categoryIds) &&
                (accountIds.isEmpty() || document.accountId in accountIds)
        }
        val limit = maxResults.coerceAtLeast(0)
        val queryTokens = query.tokens()
        return if (queryTokens.isEmpty()) {
            filteredDocuments.take(limit).mapIndexed { index, document ->
                SharedSearchMatch(document.id, relevanceScore = filteredDocuments.size - index, reason = "Local order match")
            }
        } else {
            filteredDocuments
                .mapNotNull { document -> document.match(queryTokens) }
                .sortedWith(compareByDescending<SharedSearchMatch> { it.relevanceScore }.thenBy { it.id })
                .take(limit)
        }
    }

    private fun SharedSearchDocument.match(queryTokens: Set<String>): SharedSearchMatch? {
        val titleTokens = title.tokens()
        val bodyTokens = body.tokens()
        val metadataTokens = metadata.values.joinToString(" ").tokens()
        val normalizedTitle = title.lowercase()
        val score = queryTokens.sumOf { token ->
            when {
                normalizedTitle == token -> 8
                token in titleTokens -> 6
                titleTokens.any { it.startsWith(token) } -> 4
                token in bodyTokens -> 3
                bodyTokens.any { it.startsWith(token) } -> 2
                token in metadataTokens -> 2
                else -> 0
            }
        }
        return if (score > 0) SharedSearchMatch(id, score, "Matched local transaction text") else null
    }

    private fun String?.tokens(): Set<String> =
        orEmpty()
            .lowercase()
            .split(Regex("[^a-z0-9]+"))
            .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
            .toSet()
}