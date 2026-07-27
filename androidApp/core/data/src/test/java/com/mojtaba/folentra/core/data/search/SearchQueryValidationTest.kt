package com.mojtaba.folentra.core.data.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryValidationTest {
    @Test
    fun `valid query has no validation errors`() {
        val query = SearchQuery(
            text = "coffee",
            filters = SearchFilters(
                transactionTypes = setOf(SearchTransactionType.Expense),
                categoryIds = setOf("food"),
                tagIds = setOf("morning"),
                dateRange = SearchDateRange(startMillis = 1_000L, endMillis = 2_000L),
                amountRange = SearchAmountRange(minMinor = 100L, maxMinor = 500L),
                currencyCode = "USD",
                recurring = SearchRecurringFilter.NonRecurringOnly,
            ),
            sort = SearchSort.AmountDescending,
        )

        assertTrue(query.validate().isValid)
    }

    @Test
    fun `text longer than max length is invalid after normalization`() {
        val query = SearchQuery(text = "a".repeat(SearchQuery.MAX_TEXT_LENGTH + 1))

        assertEquals(
            setOf(SearchQueryValidationError.TextTooLong),
            query.validate().errors,
        )
    }

    @Test
    fun `invalid currency is invalid`() {
        val query = SearchQuery(
            filters = SearchFilters(currencyCode = "US1"),
        )

        assertEquals(
            setOf(SearchQueryValidationError.InvalidCurrencyCode),
            query.validate().errors,
        )
    }

    @Test
    fun `blank currency is valid because it normalizes to no currency filter`() {
        val query = SearchQuery(
            filters = SearchFilters(currencyCode = " "),
        )

        assertTrue(query.validate().isValid)
    }

    @Test
    fun `invalid date range is invalid`() {
        val query = SearchQuery(
            filters = SearchFilters(
                dateRange = SearchDateRange(startMillis = 2_000L, endMillis = 1_000L),
            ),
        )

        assertEquals(
            setOf(SearchQueryValidationError.InvalidDateRange),
            query.validate().errors,
        )
    }

    @Test
    fun `negative amount range is invalid`() {
        val query = SearchQuery(
            filters = SearchFilters(
                amountRange = SearchAmountRange(minMinor = -1L),
            ),
        )

        assertEquals(
            setOf(SearchQueryValidationError.NegativeAmountRange),
            query.validate().errors,
        )
    }

    @Test
    fun `amount minimum greater than maximum is invalid`() {
        val query = SearchQuery(
            filters = SearchFilters(
                amountRange = SearchAmountRange(minMinor = 500L, maxMinor = 100L),
            ),
        )

        assertEquals(
            setOf(SearchQueryValidationError.InvalidAmountRange),
            query.validate().errors,
        )
    }

    @Test
    fun `blank category and tag ids are invalid before normalization`() {
        val query = SearchQuery(
            filters = SearchFilters(
                categoryIds = setOf("food", " "),
                tagIds = setOf(""),
            ),
        )

        assertEquals(
            setOf(
                SearchQueryValidationError.BlankCategoryId,
                SearchQueryValidationError.BlankTagId,
            ),
            query.validate().errors,
        )
    }

    @Test
    fun `blank category and tag ids are removed by normalization`() {
        val query = SearchQuery(
            filters = SearchFilters(
                categoryIds = setOf("food", " "),
                tagIds = setOf("", "tax"),
            ),
        )

        val normalized = query.normalized()

        assertEquals(setOf("food"), normalized.filters.categoryIds)
        assertEquals(setOf("tax"), normalized.filters.tagIds)
        assertTrue(normalized.validate().isValid)
    }

    @Test
    fun `multiple validation errors are preserved`() {
        val query = SearchQuery(
            text = "a".repeat(SearchQuery.MAX_TEXT_LENGTH + 1),
            filters = SearchFilters(
                currencyCode = "US1",
                dateRange = SearchDateRange(startMillis = 2L, endMillis = 1L),
                amountRange = SearchAmountRange(minMinor = -1L, maxMinor = -2L),
            ),
        )

        assertFalse(query.validate().isValid)
        assertEquals(
            setOf(
                SearchQueryValidationError.TextTooLong,
                SearchQueryValidationError.InvalidCurrencyCode,
                SearchQueryValidationError.InvalidDateRange,
                SearchQueryValidationError.NegativeAmountRange,
                SearchQueryValidationError.InvalidAmountRange,
            ),
            query.validate().errors,
        )
    }
}
