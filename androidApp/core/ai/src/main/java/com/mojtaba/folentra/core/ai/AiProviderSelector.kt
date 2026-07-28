package com.mojtaba.folentra.core.ai

import com.mojtaba.folentra.core.featureflags.DefaultFeatureFlags
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator
import kotlinx.coroutines.CancellationException

class AiProviderSelector(
    private val providers: List<AiProvider>,
    private val featureFlags: FeatureFlagEvaluator,
) {
    suspend fun selectFor(capability: AiCapability): AiProvider {
        if (!isEnabled(capability)) {
            return provider(AiProviderType.NoOp) ?: NoOpAiProvider
        }

        return providers
            .asSequence()
            .filterNot { it.type == AiProviderType.NoOp || it.type == AiProviderType.RuleBased }
            .filter { it.capabilities.supports(capability) }
            .firstOrNull { it.safeCurrentAvailability() is AiProviderAvailability.Available }
            ?: provider(AiProviderType.RuleBased)
            ?: RuleBasedAiProvider
    }

    suspend fun isAvailable(capability: AiCapability): Boolean =
        selectFor(capability).let { provider ->
            provider.type != AiProviderType.NoOp &&
                provider.capabilities.supports(capability) &&
                provider.safeCurrentAvailability() is AiProviderAvailability.Available
        }

    private fun isEnabled(capability: AiCapability): Boolean =
        when (capability) {
            AiCapability.MonthlySummary -> featureFlags.isEnabled(DefaultFeatureFlags.AiInsightsEnabled)
            AiCapability.SemanticSearch -> featureFlags.isEnabled(DefaultFeatureFlags.SemanticSearchEnabled)
            AiCapability.SmartAutofill -> featureFlags.isEnabled(DefaultFeatureFlags.SmartAutofillEnabled)
        }

    private fun provider(type: AiProviderType): AiProvider? =
        providers.firstOrNull { it.type == type }

    private suspend fun AiProvider.safeCurrentAvailability(): AiProviderAvailability =
        try {
            currentAvailability()
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            AiProviderAvailability.Unavailable(throwable.message ?: "${type.name} availability check failed.")
        }
}
