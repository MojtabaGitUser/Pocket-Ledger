package com.mojtaba.pocketledger.core.data.search

import java.util.Locale

data class SearchQuery(
    val text: String = "",
    val filters: SearchFilters = SearchFilters(),
    val sort: SearchSort = SearchSort.DateDescending,
) {
    val isEmpty: Boolean
        get() = text.isBlank() && filters.isEmpty

    fun normalized(): SearchQuery =
        copy(
            text = text.normalizedSearchText(),
            filters = filters.normalized(),
        )

    fun validate(): SearchQueryValidationResult =
        SearchQueryValidation.validate(this)

    companion object {
        const val MAX_TEXT_LENGTH = 120
    }
}

private fun SearchFilters.normalized(): SearchFilters =
    copy(
        transactionTypes = transactionTypes.sortedBy { it.ordinal }.toSet(),
        categoryIds = categoryIds.normalizedIds(),
        tagIds = tagIds.normalizedIds(),
        currencyCode = currencyCode
            ?.trim()
            ?.uppercase(Locale.US)
            ?.takeIf { it.isNotEmpty() },
    )

private fun String.normalizedSearchText(): String =
    trim().replace(Regex("\\s+"), " ")

private fun Set<String>.normalizedIds(): Set<String> =
    mapNotNull { id ->
        id.trim().takeIf { it.isNotEmpty() }
    }
        .sorted()
        .toSet()
