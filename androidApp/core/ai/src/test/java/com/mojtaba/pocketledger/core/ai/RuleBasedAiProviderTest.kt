package com.mojtaba.pocketledger.core.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedAiProviderTest {
    @Test
    fun monthlySummaryHandlesEmptyData() = runTest {
        val result = RuleBasedAiProvider.generateMonthlySummary(emptyMonthlyRequest())

        assertTrue(result is AiInferenceResult.Success)
        result as AiInferenceResult.Success
        assertEquals(AiProviderType.RuleBased, result.providerType)
        assertTrue(result.value.summaryText.contains("No local transactions"))
        assertEquals(AiResultQuality.Low, result.value.quality)
    }

    @Test
    fun monthlySummaryReportsNormalAggregateData() = runTest {
        val result = RuleBasedAiProvider.generateMonthlySummary(
            emptyMonthlyRequest().copy(
                totalIncomeMinor = 250_000,
                totalExpenseMinor = 125_000,
                transactionCount = 8,
                categorySummaries = listOf(AiCategorySummary("groceries", "Groceries", 75_000, 4)),
            ),
        )

        assertTrue(result is AiInferenceResult.Success)
        result as AiInferenceResult.Success
        assertTrue(result.value.summaryText.contains("positive"))
        assertTrue(result.value.insights.any { it.contains("Groceries") })
    }

    @Test
    fun semanticSearchRanksExactTitleBeforeBodyMatch() = runTest {
        val result = RuleBasedAiProvider.semanticSearch(
            SemanticSearchRequest(
                query = "coffee",
                documents = listOf(
                    SemanticSearchDocument("body", title = "Cafe", body = "coffee refill"),
                    SemanticSearchDocument("title", title = "coffee", body = "snack"),
                ),
            ),
        )

        assertTrue(result is AiInferenceResult.Success)
        result as AiInferenceResult.Success
        assertEquals(listOf("title", "body"), result.value.rankedIds)
    }

    @Test
    fun smartAutofillSuggestsCategoryFromLocalPattern() = runTest {
        val result = RuleBasedAiProvider.smartAutofill(
            SmartAutofillRequest(
                partialInput = SmartAutofillInput(description = "Market", transactionType = "expense"),
                candidates = SmartAutofillCandidates(
                    categories = listOf(SmartAutofillCategory("grocery", "Groceries", "expense")),
                ),
                history = listOf(
                    SmartAutofillHistoryItem(
                        transactionId = "1",
                        description = "Market Basket",
                        transactionType = "expense",
                        categoryId = "grocery",
                        amountMinor = 4200,
                        occurredAtMillis = 1L,
                    ),
                ),
                occurredAtMillis = 2L,
            ),
        )

        assertTrue(result is AiInferenceResult.Success)
        result as AiInferenceResult.Success
        assertNotNull(result.value.suggestion)
        assertEquals("grocery", result.value.suggestion?.categoryId)
    }

    private fun emptyMonthlyRequest(): MonthlySummaryRequest = MonthlySummaryRequest(
        periodLabel = "July 2026",
        startMillis = 0L,
        endMillis = 1L,
        currencyCode = "USD",
        totalIncomeMinor = 0L,
        totalExpenseMinor = 0L,
        transactionCount = 0,
    )
}
