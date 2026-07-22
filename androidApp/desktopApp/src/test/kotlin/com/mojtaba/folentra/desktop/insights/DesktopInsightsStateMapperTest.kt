package com.mojtaba.folentra.desktop.insights

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DesktopInsightsStateMapperTest {
    private val mapper = DesktopInsightsStateMapper(RuleBasedDesktopInsightsProvider())

    @Test
    fun mapsSampleDataToContentState() {
        val state = mapper.map(SampleDesktopInsightsDataSource.monthlySummary())

        val content = assertIs<DesktopInsightsUiState.Content>(state)
        assertEquals("February 2026", content.periodLabel)
        assertEquals("USD 3680.00", content.incomeText)
        assertEquals("USD 2568.85", content.expenseText)
        assertEquals("USD 1111.15", content.netText)
        assertEquals(DesktopProviderStatus.RuleBasedFallback, content.result.providerStatus)
        assertTrue(content.result.insights.isNotEmpty())
        assertEquals("Housing", content.topCategories.first().label)
    }

    @Test
    fun mapsNoActivityToEmptyState() {
        val state = mapper.map(
            DesktopMonthlySummaryInput(
                periodLabel = "March 2026",
                currencyCode = "USD",
                totalIncomeMinor = 0,
                totalExpenseMinor = 0,
                transactionCount = 0,
                categories = emptyList(),
                budgets = emptyList(),
                recurringHints = emptyList(),
            ),
        )

        assertEquals(DesktopInsightsUiState.Empty("March 2026"), state)
    }

    @Test
    fun mapsProviderFailureToErrorStateWithoutProviderInternals() {
        val state = DesktopInsightsStateMapper(
            provider = object : DesktopInsightsProvider {
                override fun generate(input: DesktopMonthlySummaryInput): DesktopMonthlyInsightResult {
                    error("stack trace with private provider detail")
                }
            },
        ).map(SampleDesktopInsightsDataSource.monthlySummary())

        assertEquals(
            DesktopInsightsUiState.Error("Could not generate desktop insights."),
            state,
        )
    }
}
