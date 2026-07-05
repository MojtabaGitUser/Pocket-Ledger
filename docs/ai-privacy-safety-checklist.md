# AI Privacy And Safety Checklist

Pocket Ledger E-12 implements private AI feature contracts with local-only provider selection. This checklist must be reviewed before every release that changes Insights, semantic search, smart autofill, provider selection, analytics, logging, or Play Store declarations.

## Required Guarantees

- AI runs on-device or uses deterministic local rule-based fallback only.
- No remote AI provider is used.
- No OpenAI, Gemini cloud, Anthropic, remote LLM, or cloud model invocation is configured.
- No API keys, model credentials, service accounts, or AI secrets are required for E-12.
- No financial data leaves the device for AI processing.
- Raw transaction notes, descriptions, merchant names, category names, account names, exact balances, and exact amounts are not logged.
- Exact amounts and exact balances are not sent to analytics.
- Product events for AI must use only approved typed enums, result values, feature flag state, count buckets, or amount buckets.
- Smart autofill suggestions require user confirmation before changing the form.
- Smart autofill must not save or submit a transaction automatically.
- Monthly summaries are descriptive local summaries and are not investment, debt, tax, legal, medical, or regulated financial advice.
- Rule-based fallback is available offline and does not require an AI runtime.
- Provider failures fall back without crashing core flows.
- Insights screen exposes no prompts, stack traces, raw provider errors, or hidden diagnostics.
- Insights screen uses headings, state descriptions, and non-color-only provider status.
- 200% font scaling must be checked when E-12 UI changes.
- Debug diagnostics are not visible in release builds.
- Privacy policy and Play Store readiness docs must remain accurate after behavior changes.

## Current Provider State

- `GeminiNanoAiProvider` is a compile-safe on-device provider shell and is unavailable by default.
- `MlKitAiProvider` is a compile-safe local provider shell for future semantic support and is unavailable by default.
- `RuleBasedAiProvider` implements monthly summaries, semantic search ranking, and smart autofill with deterministic local logic.
- `AiProviderSelector` prefers an available on-device provider before rule-based fallback.
- `AiFallbackStrategy` returns fallback results when on-device providers are unavailable, unsupported, disabled, or fail.

## Feature Data Boundaries

- Monthly summary requests use aggregate totals, category summaries, recurring hints, and budget comparisons.
- Semantic search may use local transaction text fields inside the app process only; no remote provider receives them.
- Smart autofill may use local merchant/note history inside the app process only; suggestions are explicit and dismissible.
- No AI request path writes raw financial text to logs or analytics.

## Future On-Device Model Requirements

Before enabling a real model runtime:

- Keep inference fully local and offline-capable.
- Add capability and health checks that do not block app startup.
- Do not download models at runtime unless a documented private model asset strategy exists.
- Sanitize prompts and structured inputs.
- Add tests proving fallback still works when runtime initialization or inference fails.
- Update privacy policy, Play Store readiness, release checklist, and this checklist.
