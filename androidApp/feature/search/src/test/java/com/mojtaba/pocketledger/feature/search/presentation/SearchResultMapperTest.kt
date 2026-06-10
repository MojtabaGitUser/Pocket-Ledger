package com.mojtaba.pocketledger.feature.search.presentation

import com.mojtaba.pocketledger.core.testing.fixture.TestClock
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerCategory
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerTag
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class SearchResultMapperTest {
    @Test
    fun mapsTransactionToSearchResultUiModel() {
        val result = SearchResultMapper.map(
            transaction = testLedgerTransaction(
                id = "transaction-1",
                amountMinor = -4_250,
                merchant = "Coffee Shop",
                note = "Team breakfast",
                occurredAt = TestClock.November14,
            ),
            category = testLedgerCategory(id = "food", name = "Food"),
            tags = listOf(testLedgerTag(id = "work", name = "Work")),
            zoneId = ZoneId.of("UTC"),
        )

        assertEquals("transaction-1", result.transactionId)
        assertEquals("Coffee Shop", result.title)
        assertEquals("-${'$'}42.50", result.amount.text)
        assertEquals("Expense", result.typeLabel)
        assertEquals("Food", result.categoryLabel)
        assertEquals("Nov 14, 2023", result.dateLabel)
        assertEquals("Team breakfast", result.notePreview)
        assertEquals(listOf("Work"), result.tagLabels)
        assertTrue(result.contentDescription.contains("Coffee Shop"))
    }
}
