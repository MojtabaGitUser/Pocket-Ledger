package com.mojtaba.pocketledger.core.ai

import com.mojtaba.pocketledger.core.featureflags.DefaultFeatureFlags
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator

class AiProviderSelector(
    private val providers: List<AiProvider>,
    private val featureFlags: FeatureFlagEvaluator,
) {
    fun selectFor(capability: AiCapability): AiProvider {
        if (!isEnabled(capability)) {
            return provider(AiProviderType.NoOp) ?: NoOpAiProvider
        }

        return providers
            .asSequence()
            .filterNot { it.type == AiProviderType.NoOp || it.type == AiProviderType.RuleBased }
            .filter { it.capabilities.supports(capability) }
            .firstOrNull { it.safeAvailability() is AiProviderAvailability.Available }
            ?: provider(AiProviderType.RuleBased)
            ?: RuleBasedAiProvider
    }

    fun isAvailable(capability: AiCapability): Boolean =
        selectFor(capability).let { provider ->
            provider.type != AiProviderType.NoOp &&
                provider.capabilities.supports(capability) &&
                provider.safeAvailability() is AiProviderAvailability.Available
        }

    private fun isEnabled(capability: AiCapability): Boolean =
        when (capability) {
            AiCapability.MonthlySummary -> featureFlags.isEnabled(DefaultFeatureFlags.AiInsightsEnabled)
            AiCapability.SemanticSearch -> featureFlags.isEnabled(DefaultFeatureFlags.SemanticSearchEnabled)
            AiCapability.SmartAutofill -> featureFlags.isEnabled(DefaultFeatureFlags.SmartAutofillEnabled)
        }

    private fun provider(type: AiProviderType): AiProvider? =
        providers.firstOrNull { it.type == type }

    private fun AiProvider.safeAvailability(): AiProviderAvailability =
        try {
            availability()
        } catch (throwable: Throwable) {
            AiProviderAvailability.Unavailable(throwable.message ?: "${type.name} availability check failed.")
        }
}
