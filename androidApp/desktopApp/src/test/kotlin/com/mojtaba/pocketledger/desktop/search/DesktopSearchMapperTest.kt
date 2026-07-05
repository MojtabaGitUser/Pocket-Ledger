package com.mojtaba.pocketledger.desktop.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DesktopSearchMapperTest {
    private val mapper = DesktopSearchMapper()
    private val records = SampleDesktopSearchDataSource.records()

    @Test
    fun blankQueryReturnsRecentSampleResults() {
        val state = mapper.map(records, DesktopSearchQuery())

        assertFalse(state.isEmptyLedger)
        assertTrue(state.results.isNotEmpty())
        assertEquals(DesktopSearchProviderStatus.KeywordOnly, state.providerStatus)
        assertNotNull(state.selectedResult)
    }

    @Test
    fun keywordQueryMatchesTitleAndCategory() {
        val state = mapper.map(records, DesktopSearchQuery(text = "grocery"))

        assertEquals(listOf("demo-search-groceries-a"), state.results.map { it.id })
        assertEquals("Keyword match", state.results.single().matchReason)
    }

    @Test
    fun typeFilterLimitsResults() {
        val state = mapper.map(
            records = records,
            query = DesktopSearchQuery(transactionTypes = setOf(DesktopSearchTransactionType.Income)),
        )

        assertTrue(state.results.isNotEmpty())
        assertTrue(state.results.all { it.typeLabel == "Income" })
    }

    @Test
    fun semanticModeUsesLocalFallbackStatus() {
        val state = mapper.map(records, DesktopSearchQuery(text = "trans", mode = DesktopSearchMode.Semantic))

        assertEquals(DesktopSearchProviderStatus.LocalSemanticFallback, state.providerStatus)
        assertTrue(state.results.any { it.id == "demo-search-transit" })
        assertTrue(state.results.all { it.matchReason == "Local semantic fallback match" })
    }

    @Test
    fun emptyLedgerMapsToEmptyState() {
        val state = mapper.map(emptyList(), DesktopSearchQuery(text = "rent"))

        assertTrue(state.isEmptyLedger)
        assertTrue(state.results.isEmpty())
        assertEquals(null, state.selectedResult)
    }
}
