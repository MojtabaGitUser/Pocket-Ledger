package com.mojtaba.folentra.core.ai

import com.mojtaba.folentra.core.featureflags.DefaultFeatureFlags
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.folentra.core.featureflags.FeatureFlagKey
import com.mojtaba.folentra.core.featureflags.FeatureFlagValue
import com.mojtaba.folentra.core.featureflags.LocalFeatureFlagProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiProviderSelectorTest {
    @Test
    fun disabledFeatureFlagSelectsNoOpProvider() {
        val selector = selector(
            providers = listOf(availableProvider(AiProviderType.GeminiNano), RuleBasedAiProvider, NoOpAiProvider),
            enabledFlags = mapOf(
                DefaultFeatureFlags.AiInsightsEnabled.key to FeatureFlagValue.BooleanValue(false),
                DefaultFeatureFlags.SemanticSearchEnabled.key to FeatureFlagValue.BooleanValue(false),
            ),
        )

        assertEquals(AiProviderType.NoOp, selector.selectFor(AiCapability.MonthlySummary).type)
        assertEquals(AiProviderType.NoOp, selector.selectFor(AiCapability.SemanticSearch).type)
    }

    @Test
    fun supportedDeviceSelectsPreferredAiProvider() {
        val selector = selector(
            providers = listOf(availableProvider(AiProviderType.GeminiNano), RuleBasedAiProvider, NoOpAiProvider),
            enabledFlags = mapOf(DefaultFeatureFlags.AiInsightsEnabled.key to FeatureFlagValue.BooleanValue(true)),
        )

        assertEquals(AiProviderType.GeminiNano, selector.selectFor(AiCapability.MonthlySummary).type)
    }

    @Test
    fun unsupportedDeviceFallsBackToRuleBasedProvider() {
        val selector = selector(
            providers = listOf(unavailableProvider(AiProviderType.GeminiNano), RuleBasedAiProvider, NoOpAiProvider),
            enabledFlags = mapOf(DefaultFeatureFlags.AiInsightsEnabled.key to FeatureFlagValue.BooleanValue(true)),
        )

        assertEquals(AiProviderType.RuleBased, selector.selectFor(AiCapability.MonthlySummary).type)
    }

    @Test
    fun providerFailureFallsBackToRuleBasedProvider() = runTest {
        val selector = selector(
            providers = listOf(failingProvider(AiProviderType.GeminiNano), RuleBasedAiProvider, NoOpAiProvider),
            enabledFlags = mapOf(DefaultFeatureFlags.AiInsightsEnabled.key to FeatureFlagValue.BooleanValue(true)),
        )
        val strategy = AiFallbackStrategy(selector)

        val result = strategy.generateSummary(AiSummaryRequest(periodLabel = "June", facts = listOf("Local fact.")))

        assertTrue(result is AiInferenceResult.Success)
        result as AiInferenceResult.Success
        assertEquals(AiProviderType.RuleBased, result.providerType)
        assertEquals("Local fact.", result.value.text)
        assertEquals("GeminiNano failed.", result.fallbackReason)
    }

    private fun selector(
        providers: List<AiProvider>,
        enabledFlags: Map<FeatureFlagKey, FeatureFlagValue>,
    ): AiProviderSelector =
        AiProviderSelector(
            providers = providers,
            featureFlags = FeatureFlagEvaluator(LocalFeatureFlagProvider(enabledFlags)),
        )

    private fun availableProvider(type: AiProviderType): AiProvider =
        FakeAiProvider(type = type, availability = AiProviderAvailability.Available)

    private fun unavailableProvider(type: AiProviderType): AiProvider =
        FakeAiProvider(type = type, availability = AiProviderAvailability.Unavailable("unsupported"))

    private fun failingProvider(type: AiProviderType): AiProvider =
        FakeAiProvider(type = type, availability = AiProviderAvailability.Available, fail = true)

    private class FakeAiProvider(
        override val type: AiProviderType,
        private val availability: AiProviderAvailability,
        private val fail: Boolean = false,
    ) : AiProvider {
        override val capabilities: AiProviderCapabilities = AiProviderCapabilities.LocalFinanceFeatures

        override fun availability(): AiProviderAvailability = availability

        override suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult> {
            if (fail) error("${type.name} failed.")
            return AiInferenceResult.Success(AiSummaryResult("AI summary"), type)
        }

        override suspend fun generateMonthlySummary(
            request: MonthlySummaryRequest,
        ): AiInferenceResult<MonthlySummaryResult> {
            if (fail) error("${type.name} failed.")
            return AiInferenceResult.Success(
                MonthlySummaryResult(
                    title = "AI summary",
                    summaryText = "AI summary",
                    insights = emptyList(),
                    providerType = type,
                ),
                type,
            )
        }

        override suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> {
            if (fail) error("${type.name} failed.")
            return AiInferenceResult.Success(SemanticSearchResult(request.documents.map { it.id }), type)
        }

        override suspend fun smartAutofill(request: SmartAutofillRequest): AiInferenceResult<SmartAutofillResult> {
            if (fail) error("${type.name} failed.")
            return AiInferenceResult.Success(SmartAutofillResult(null, type, AiResultQuality.Low), type)
        }
    }
}
