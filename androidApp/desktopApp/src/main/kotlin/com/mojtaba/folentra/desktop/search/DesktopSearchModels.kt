package com.mojtaba.folentra.desktop.search

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

data class DesktopSearchRecord(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val type: DesktopSearchTransactionType,
    val occurredAtMillis: Long,
    val category: String,
    val notePreview: String?,
    val tags: List<String> = emptyList(),
    val recurring: Boolean = false,
)

data class DesktopSearchQuery(
    val text: String = "",
    val mode: DesktopSearchMode = DesktopSearchMode.Keyword,
    val transactionTypes: Set<DesktopSearchTransactionType> = emptySet(),
) {
    val isEmpty: Boolean
        get() = text.isBlank() && transactionTypes.isEmpty()

    fun normalized(): DesktopSearchQuery = copy(text = text.trim().replace(Regex("\\s+"), " "))
}

data class DesktopSearchResult(
    val id: String,
    val title: String,
    val amountText: String,
    val amountDescription: String,
    val typeLabel: String,
    val categoryLabel: String,
    val dateLabel: String,
    val notePreview: String?,
    val tags: List<String>,
    val recurringLabel: String?,
    val matchReason: String,
    val contentDescription: String,
)

data class DesktopSearchUiState(
    val query: DesktopSearchQuery,
    val results: List<DesktopSearchResult>,
    val selectedResult: DesktopSearchResult?,
    val isEmptyLedger: Boolean,
    val providerStatus: DesktopSearchProviderStatus,
) {
    val hasNoResults: Boolean
        get() = !isEmptyLedger && results.isEmpty()
}

enum class DesktopSearchMode {
    Keyword,
    Semantic,
}

enum class DesktopSearchTransactionType(val label: String) {
    Income("Income"),
    Expense("Expense"),
}

enum class DesktopSearchProviderStatus {
    KeywordOnly,
    LocalSemanticFallback,
}

class DesktopSearchMapper(
    private val zoneId: ZoneId = ZoneId.of("UTC"),
) {
    fun map(
        records: List<DesktopSearchRecord>,
        query: DesktopSearchQuery,
        selectedId: String? = null,
    ): DesktopSearchUiState {
        val normalized = query.normalized()
        val ranked = records
            .asSequence()
            .filter { record -> normalized.transactionTypes.isEmpty() || record.type in normalized.transactionTypes }
            .mapNotNull { record -> record.match(normalized)?.let { match -> record to match } }
            .sortedWith(
                compareByDescending<Pair<DesktopSearchRecord, SearchMatch>> { it.second.score }
                    .thenByDescending { it.first.occurredAtMillis }
                    .thenBy { it.first.id },
            )
            .map { (record, match) -> record.toResult(match.reason) }
            .toList()
        val selected = ranked.firstOrNull { it.id == selectedId } ?: ranked.firstOrNull()
        return DesktopSearchUiState(
            query = normalized,
            results = ranked,
            selectedResult = selected,
            isEmptyLedger = records.isEmpty(),
            providerStatus = when (normalized.mode) {
                DesktopSearchMode.Keyword -> DesktopSearchProviderStatus.KeywordOnly
                DesktopSearchMode.Semantic -> DesktopSearchProviderStatus.LocalSemanticFallback
            },
        )
    }

    private fun DesktopSearchRecord.match(query: DesktopSearchQuery): SearchMatch? {
        val text = query.text.trim().lowercase(Locale.US)
        if (text.isBlank()) {
            return SearchMatch(score = 1, reason = "Recent sample activity")
        }
        val tokens = text.split(Regex("[^a-z0-9]+"))
            .mapNotNull { it.takeIf(String::isNotBlank) }
            .toSet()
        val titleTokens = title.tokens()
        val categoryTokens = category.tokens()
        val noteTokens = notePreview.tokens()
        val tagTokens = tags.joinToString(" ").tokens()
        val score = tokens.sumOf { token ->
            when {
                title.lowercase(Locale.US).contains(token) -> 8
                token in categoryTokens -> 5
                token in tagTokens -> 4
                token in noteTokens -> 3
                query.mode == DesktopSearchMode.Semantic && titleTokens.any { it.startsWith(token) } -> 3
                query.mode == DesktopSearchMode.Semantic && noteTokens.any { it.startsWith(token) } -> 2
                else -> 0
            }
        }
        if (score <= 0) return null
        val reason = if (query.mode == DesktopSearchMode.Semantic) {
            "Local semantic fallback match"
        } else {
            "Keyword match"
        }
        return SearchMatch(score, reason)
    }

    private fun DesktopSearchRecord.toResult(matchReason: String): DesktopSearchResult {
        val amount = formatAmount(amountMinor, currencyCode)
        val date = Instant.ofEpochMilli(occurredAtMillis).atZone(zoneId).format(DateFormatter)
        val recurringLabel = if (recurring) "Recurring" else null
        return DesktopSearchResult(
            id = id,
            title = title,
            amountText = amount,
            amountDescription = "$amount ${type.label.lowercase(Locale.US)}",
            typeLabel = type.label,
            categoryLabel = category,
            dateLabel = date,
            notePreview = notePreview,
            tags = tags,
            recurringLabel = recurringLabel,
            matchReason = matchReason,
            contentDescription = listOfNotNull(
                "Sample transaction $title",
                type.label,
                category,
                date,
                notePreview,
                "$amount ${type.label.lowercase(Locale.US)}",
                recurringLabel,
                tags.takeIf { it.isNotEmpty() }?.joinToString(prefix = "Tags "),
            ).joinToString(separator = ", "),
        )
    }
}

private data class SearchMatch(
    val score: Int,
    val reason: String,
)

private fun String?.tokens(): Set<String> =
    orEmpty()
        .lowercase(Locale.US)
        .split(Regex("[^a-z0-9]+"))
        .mapNotNull { it.takeIf(String::isNotBlank) }
        .toSet()

private fun formatAmount(amountMinor: Long, currencyCode: String): String {
    val sign = when {
        amountMinor > 0L -> "+"
        amountMinor < 0L -> "-"
        else -> ""
    }
    return String.format(Locale.US, "%s%s %.2f", sign, currencyCode.uppercase(Locale.US), abs(amountMinor) / 100.0)
}
