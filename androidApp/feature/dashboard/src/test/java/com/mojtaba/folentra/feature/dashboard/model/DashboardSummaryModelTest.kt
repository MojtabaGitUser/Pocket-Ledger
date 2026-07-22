package com.mojtaba.folentra.feature.dashboard.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DashboardSummaryModelTest {
    @Test
    fun periodContainsInclusiveBounds() {
        val period = DashboardPeriod(
            startMillis = 100L,
            endMillis = 200L,
            label = "Test",
        )

        assertEquals(true, period.contains(100L))
        assertEquals(true, period.contains(150L))
        assertEquals(true, period.contains(200L))
        assertEquals(false, period.contains(99L))
        assertEquals(false, period.contains(201L))
    }

    @Test
    fun invalidPeriodIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            DashboardPeriod(
                startMillis = 200L,
                endMillis = 100L,
                label = "Invalid",
            )
        }
    }

    @Test
    fun summariesUseValueSemantics() {
        val cashFlow = CashFlowSummary(
            incomeMinor = 100L,
            expenseMinor = 40L,
            netMinor = 60L,
            currencyCode = "USD",
        )
        val summary = DashboardSummary(
            period = DashboardPeriod(1L, 2L, "Period"),
            cashFlow = cashFlow,
            topCategories = emptyList(),
            budgetProgress = emptyList(),
            recentTransactions = emptyList(),
            insights = listOf(DashboardInsight.NoData),
            generatedAt = 3L,
        )

        assertEquals(summary, summary.copy())
        assertEquals(70L, cashFlow.copy(netMinor = 70L).netMinor)
    }
}
