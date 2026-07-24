# AI Architecture

Folentra E-12 uses a local-only AI provider abstraction in `:core:ai`.

## Provider Contracts

The public contracts cover:

- Private monthly summary: `MonthlySummaryRequest` and `MonthlySummaryResult`.
- Semantic search: `SemanticSearchRequest` and `SemanticSearchResult`.
- Smart autofill: `SmartAutofillRequest` and `SmartAutofillResult`.

Contracts are strongly typed and avoid exposing database entities directly. Feature modules pass local domain data or aggregates into `:core:ai`; `:core:ai` does not depend on Firebase, Room, network SDKs, or feature modules.

## Provider Selection

`AiProviderSelector` chooses providers in this order when a feature flag is enabled:

1. Available on-device provider with the requested capability.
2. Deterministic `RuleBasedAiProvider` fallback.
3. `NoOpAiProvider` only when the feature is explicitly disabled.

`AiFallbackStrategy` wraps provider execution and falls back to the rule-based provider if an on-device provider is unavailable, unsupported, unhealthy, or fails.

## Current Provider Status

`MlKitAiProvider` is backed by the ML Kit GenAI Prompt API and performs real on-device Gemini Nano inference for general and monthly summaries. It checks AICore model status before inference, downloads the shared model when the device reports it as downloadable, and contains SDK, quota, timeout, and unsupported-device failures. `RuleBasedAiProvider` remains the deterministic fallback. The legacy `GeminiNanoAiProvider` shell is retained for source compatibility but is not registered in the application graph.

## Feature Behavior

Monthly Insights aggregate transaction totals, category totals, recurring hints, and budget comparisons before calling the provider. Summary language is descriptive and avoids regulated financial advice.

Semantic search keeps exact keyword search intact and uses provider ranking for semantic mode. The fallback ranks local documents by exact match, prefix match, token overlap, and stable IDs.

Smart autofill uses local history to suggest category, amount when repeated exactly, and recurring status. Suggestions are never applied until the user accepts them.

## Privacy Guarantees

E-12 adds no remote AI service, no API key, no cloud model invocation, and no model credential. Financial data used for AI remains in the app process. Product analytics must not receive transaction descriptions, merchant names, notes, category names, account names, exact amounts, exact balances, raw IDs, prompts, or provider diagnostics.

## Known Limitations

The current on-device provider shells are not backed by a real model runtime. Future work can plug in a real local runtime behind the same contracts after adding model asset strategy, runtime health checks, prompt/input sanitization tests, and release documentation updates.
