package com.mojtaba.pocketledger.core.testing.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LargeBenchmarkDatasetTest {
    @Test
    fun createProducesExpectedLargeDatasetShape() {
        val data = LargeBenchmarkDataset.create()

        assertEquals(10, data.categories.size)
        assertEquals(48, data.budgets.size)
        assertEquals(8, data.tags.size)
        assertEquals(6_000, data.transactions.size)
        assertTrue(data.transactionTagLinks.size in 2_300..2_500)
        assertTrue(data.transactions.any { it.type == LargeBenchmarkDataset.IncomeType })
        assertTrue(data.transactions.any { it.type == LargeBenchmarkDataset.ExpenseType })
        assertTrue(data.transactions.any { it.merchant == "LedgerMart Market 0000" })
        assertTrue(data.transactions.any { it.note?.contains("ledger mart") == true })
    }

    @Test
    fun createIsDeterministic() {
        val first = LargeBenchmarkDataset.create()
        val second = LargeBenchmarkDataset.create()

        assertEquals(first, second)
    }

    @Test
    fun idsAreStableUniqueAndPrefixed() {
        val data = LargeBenchmarkDataset.create()
        val ids = buildList {
            addAll(data.categories.map { it.id })
            addAll(data.budgets.map { it.id })
            addAll(data.tags.map { it.id })
            addAll(data.transactions.map { it.id })
        }

        assertTrue(ids.all { it.startsWith(LargeBenchmarkDataset.IdPrefix) })
        assertEquals(ids.size, ids.toSet().size)
        assertEquals("large-benchmark-transaction-0000", data.transactions.first().id)
    }

    @Test
    fun relationshipsReferenceGeneratedRecords() {
        val data = LargeBenchmarkDataset.create()
        val categoryIds = data.categories.map { it.id }.toSet()
        val tagIds = data.tags.map { it.id }.toSet()
        val transactionIds = data.transactions.map { it.id }.toSet()

        assertTrue(data.budgets.all { it.categoryId in categoryIds })
        assertTrue(data.transactions.all { it.categoryId in categoryIds })
        assertTrue(data.transactionTagLinks.all { it.transactionId in transactionIds })
        assertTrue(data.transactionTagLinks.all { it.tagId in tagIds })
    }

    @Test
    fun generatedTextDoesNotUsePersonalFinancialDataExamples() {
        val data = LargeBenchmarkDataset.create()
        val generatedText = buildString {
            data.categories.forEach { appendLine(it.name) }
            data.budgets.forEach { appendLine(it.name) }
            data.tags.forEach { appendLine(it.name) }
            data.transactions.forEach {
                appendLine(it.merchant)
                appendLine(it.note)
            }
        }.lowercase()

        val disallowed = listOf(
            "account number",
            "routing number",
            "ssn",
            "starbucks",
            "chase",
            "wells fargo",
        )

        disallowed.forEach { value ->
            assertFalse("Generated text contains disallowed value: $value", generatedText.contains(value))
        }
    }
}
