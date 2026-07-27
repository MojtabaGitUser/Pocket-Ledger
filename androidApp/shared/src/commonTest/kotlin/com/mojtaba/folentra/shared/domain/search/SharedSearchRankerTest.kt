package com.mojtaba.folentra.shared.domain.search

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedSearchRankerTest {
    @Test
    fun ranksTitleMatchesBeforeBodyMatches() {
        val matches = SharedSearchRanker.rank(
            query = "market",
            maxResults = 10,
            documents = listOf(
                SharedSearchDocument(id = "body", title = "Coffee", body = "Neighborhood market"),
                SharedSearchDocument(id = "title", title = "Market", body = "Groceries"),
            ),
        )

        assertEquals(listOf("title", "body"), matches.map { it.id })
    }

    @Test
    fun filtersByCategoryBeforeRanking() {
        val matches = SharedSearchRanker.rank(
            query = "rent",
            maxResults = 10,
            categoryIds = setOf("housing"),
            documents = listOf(
                SharedSearchDocument(id = "groceries", title = "Rent", body = "", categoryId = "food"),
                SharedSearchDocument(id = "housing", title = "Rent", body = "", categoryId = "housing"),
            ),
        )

        assertEquals(listOf("housing"), matches.map { it.id })
    }
}