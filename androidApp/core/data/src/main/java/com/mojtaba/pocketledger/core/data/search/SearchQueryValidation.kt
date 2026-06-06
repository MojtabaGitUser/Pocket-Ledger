package com.mojtaba.pocketledger.core.data.search

import java.util.Locale

data class SearchQueryValidationResult(
    val errors: Set<SearchQueryValidationError>,
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}

enum class SearchQueryValidationError {
    TextTooLong,
    InvalidCurrencyCode,
    InvalidDateRange,
    NegativeAmountRange,
    InvalidAmountRange,
    BlankCategoryId,
    BlankTagId,
}

object SearchQueryValidation {
    private val currencyCodePattern = Regex("^[A-Z]{3}$")

    fun validate(query: SearchQuery): SearchQueryValidationResult {
        val errors = buildSet {
            val normalizedText = query.normalized().text
            if (normalizedText.length > SearchQuery.MAX_TEXT_LENGTH) {
                add(SearchQueryValidationError.TextTooLong)
            }

            val filters = query.filters
            validateIds(
                ids = filters.categoryIds,
                blankError = SearchQueryValidationError.BlankCategoryId,
            )
            validateIds(
                ids = filters.tagIds,
                blankError = SearchQueryValidationError.BlankTagId,
            )
            validateCurrency(filters.currencyCode)
            validateDateRange(filters.dateRange)
            validateAmountRange(filters.amountRange)
        }

        return SearchQueryValidationResult(errors)
    }

    private fun MutableSet<SearchQueryValidationError>.validateIds(
        ids: Set<String>,
        blankError: SearchQueryValidationError,
    ) {
        if (ids.any { it.isBlank() }) {
            add(blankError)
        }
    }

    private fun MutableSet<SearchQueryValidationError>.validateCurrency(currencyCode: String?) {
        val normalizedCurrency = currencyCode
            ?.trim()
            ?.uppercase(Locale.US)
            ?: return

        if (normalizedCurrency.isEmpty()) {
            return
        }

        if (!currencyCodePattern.matches(normalizedCurrency)) {
            add(SearchQueryValidationError.InvalidCurrencyCode)
        }
    }

    private fun MutableSet<SearchQueryValidationError>.validateDateRange(
        dateRange: SearchDateRange?,
    ) {
        if (dateRange != null && !dateRange.isValid()) {
            add(SearchQueryValidationError.InvalidDateRange)
        }
    }

    private fun MutableSet<SearchQueryValidationError>.validateAmountRange(
        amountRange: SearchAmountRange?,
    ) {
        if (amountRange == null) {
            return
        }

        if ((amountRange.minMinor != null && amountRange.minMinor < 0L) ||
            (amountRange.maxMinor != null && amountRange.maxMinor < 0L)
        ) {
            add(SearchQueryValidationError.NegativeAmountRange)
        }

        if (amountRange.minMinor != null &&
            amountRange.maxMinor != null &&
            amountRange.minMinor > amountRange.maxMinor
        ) {
            add(SearchQueryValidationError.InvalidAmountRange)
        }
    }
}
