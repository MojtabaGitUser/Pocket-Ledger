package com.mojtaba.folentra.core.data.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFiltersTest {
    @Test
    fun `default filters are empty`() {
        assertTrue(SearchFilters().isEmpty)
    }

    @Test
    fun `empty date and amount ranges do not make filters active`() {
        val filters = SearchFilters(
            dateRange = SearchDateRange(),
            amountRange = SearchAmountRange(),
        )

        assertTrue(filters.isEmpty)
    }

    @Test
    fun `date range makes filters active`() {
        val filters = SearchFilters(
            dateRange = SearchDateRange(startMillis = 1_000L),
        )

        assertFalse(filters.isEmpty)
    }

    @Test
    fun `amount range makes filters active`() {
        val filters = SearchFilters(
            amountRange = SearchAmountRange(maxMinor = 10_00L),
        )

        assertFalse(filters.isEmpty)
    }

    @Test
    fun `recurring filter makes filters active`() {
        val filters = SearchFilters(
            recurring = SearchRecurringFilter.RecurringOnly,
        )

        assertFalse(filters.isEmpty)
    }

    @Test
    fun `category and tag ids are trimmed sorted and blanks removed`() {
        val query = SearchQuery(
            filters = SearchFilters(
                categoryIds = setOf(" groceries ", "", "rent"),
                tagIds = setOf(" work", " ", "tax"),
            ),
        )

        val normalized = query.normalized().filters

        assertEquals(setOf("groceries", "rent"), normalized.categoryIds)
        assertEquals(setOf("tax", "work"), normalized.tagIds)
    }

    @Test
    fun `valid date range is valid`() {
        assertTrue(
            SearchDateRange(
                startMillis = 1_000L,
                endMillis = 2_000L,
            ).isValid(),
        )
    }

    @Test
    fun `invalid date range is invalid`() {
        assertFalse(
            SearchDateRange(
                startMillis = 2_000L,
                endMillis = 1_000L,
            ).isValid(),
        )
    }

    @Test
    fun `valid amount range is valid`() {
        assertTrue(
            SearchAmountRange(
                minMinor = 100L,
                maxMinor = 500L,
            ).isValid(),
        )
    }

    @Test
    fun `negative amount range is invalid`() {
        assertFalse(
            SearchAmountRange(
                minMinor = -1L,
                maxMinor = 500L,
            ).isValid(),
        )
    }

    @Test
    fun `min amount greater than max amount is invalid`() {
        assertFalse(
            SearchAmountRange(
                minMinor = 500L,
                maxMinor = 100L,
            ).isValid(),
        )
    }
}
