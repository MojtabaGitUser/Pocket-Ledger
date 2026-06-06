package com.mojtaba.pocketledger.core.data.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryTest {
    @Test
    fun `default query is empty and date descending`() {
        val query = SearchQuery()

        assertTrue(query.isEmpty)
        assertTrue(query.filters.isEmpty)
        assertEquals(SearchSort.DateDescending, query.sort)
    }

    @Test
    fun `query with text is not empty`() {
        val query = SearchQuery(text = "coffee")

        assertFalse(query.isEmpty)
    }

    @Test
    fun `query with filters is not empty`() {
        val query = SearchQuery(
            filters = SearchFilters(
                transactionTypes = setOf(SearchTransactionType.Income),
            ),
        )

        assertFalse(query.isEmpty)
    }

    @Test
    fun `text normalization trims and collapses spaces`() {
        val query = SearchQuery(text = "  grocery    store\trefund  ")

        assertEquals("grocery store refund", query.normalized().text)
    }

    @Test
    fun `currency normalization trims and uppercases code`() {
        val query = SearchQuery(
            filters = SearchFilters(currencyCode = " usd "),
        )

        assertEquals("USD", query.normalized().filters.currencyCode)
    }

    @Test
    fun `blank currency normalization becomes null`() {
        val query = SearchQuery(
            filters = SearchFilters(currencyCode = "   "),
        )

        assertEquals(null, query.normalized().filters.currencyCode)
    }

    @Test
    fun `transaction type filters preserve income and expense`() {
        val query = SearchQuery(
            filters = SearchFilters(
                transactionTypes = setOf(
                    SearchTransactionType.Expense,
                    SearchTransactionType.Income,
                ),
            ),
        )

        assertEquals(
            setOf(SearchTransactionType.Income, SearchTransactionType.Expense),
            query.normalized().filters.transactionTypes,
        )
    }

    @Test
    fun `all sort values remain stable`() {
        assertEquals(
            listOf(
                SearchSort.DateDescending,
                SearchSort.DateAscending,
                SearchSort.AmountDescending,
                SearchSort.AmountAscending,
                SearchSort.CategoryAscending,
                SearchSort.RecentlyUpdated,
            ),
            SearchSort.entries,
        )
    }
}
