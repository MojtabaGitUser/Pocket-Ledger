package com.mojtaba.pocketledger.feature.dashboard.domain

import com.mojtaba.pocketledger.core.ai.AiFallbackStrategy
import com.mojtaba.pocketledger.core.ai.AiProviderSelector
import com.mojtaba.pocketledger.core.ai.NoOpAiProvider
import com.mojtaba.pocketledger.core.ai.RuleBasedAiProvider
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.featureflags.DefaultFeatureFlags
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagValue
import com.mojtaba.pocketledger.core.featureflags.LocalFeatureFlagProvider
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardInsight
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardPeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardSummaryGeneratorTest {
    @Test
    fun aiDisabledKeepsRuleBasedDashboardSummaryAvailable() = runTest {
        val generator = generator(aiInsightsEnabled = false)

        val summary = generator.generate(input())

        assertTrue(summary.insights.any { it is DashboardInsight.PositiveCashFlow })
        assertFalse(summary.insights.any { it is DashboardInsight.AiMonthlySummary })
    }

    @Test
    fun monthlySummaryUsesProviderAbstractionWhenEnabled() = runTest {
        val generator = generator(aiInsightsEnabled = true)

        val summary = generator.generate(input())

        assertTrue(summary.insights.any { it is DashboardInsight.AiMonthlySummary })
    }

    private fun generator(aiInsightsEnabled: Boolean): DashboardSummaryGenerator {
        val featureFlags = FeatureFlagEvaluator(
            LocalFeatureFlagProvider(
                mapOf(DefaultFeatureFlags.AiInsightsEnabled.key to FeatureFlagValue.BooleanValue(aiInsightsEnabled)),
            ),
        )
        val selector = AiProviderSelector(
            providers = listOf(RuleBasedAiProvider, NoOpAiProvider),
            featureFlags = featureFlags,
        )
        return DashboardSummaryGenerator(AiFallbackStrategy(selector))
    }

    private fun input(): DashboardSummaryInput =
        DashboardSummaryInput(
            period = DashboardPeriod(startMillis = 1L, endMillis = 10L, label = "June"),
            currencyCode = "USD",
            transactions = listOf(
                LedgerTransaction(
                    id = "income",
                    amountMinor = 200_00,
                    currencyCode = "USD",
                    type = "income",
                    occurredAt = 5L,
                    merchant = "Employer",
                    source = "manual",
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            ),
            generatedAt = 11L,
        )
}
