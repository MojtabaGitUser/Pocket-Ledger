# Product Event Taxonomy

Pocket Ledger defines product events in `:core:analytics` so future analytics,
observability, crash triage, app-health reporting, and release monitoring use a
single privacy-safe vocabulary. The taxonomy is a contract only; it does not
turn on production analytics collection by itself.

## Runtime Boundary

Feature and app code should depend on `ProductAnalyticsLogger` and typed
`ProductEvent` definitions. They must not call Firebase Analytics, Crashlytics,
Logcat, or provider SDKs directly.

Current behavior:

- Debug builds construct a debug analytics sink that maps typed events and sends
  only sanitized event names and approved parameters through the existing safe
  logger if an event is logged in future work.
- Release and benchmark builds use no-op product analytics behavior.
- Firebase Analytics is present as an app dependency, but product event logging
  is not wired to Firebase in this task.
- App Distribution remains a CI/CD workflow concern and is not invoked from app
  runtime code.

## Naming Rules

Event names must be stable, readable, and provider-compatible:

- Use lower snake case: `screen_viewed`, `transaction_created`.
- Use past-tense or lifecycle wording for completed observations.
- Keep names generic and product-level, not user-specific.
- Do not include account names, category names, merchant names, notes, search
  text, exact amounts, emails, device identifiers, tokens, stack traces,
  database IDs, Firebase IDs, or CI secret names.

Parameter keys follow the same lower snake case rule and are defined by
`ProductEventParameterKey`.

## Event Categories

Approved event groups in `ProductEvent` include:

- App lifecycle: `app_opened`, `app_foregrounded`.
- Navigation and screens: `screen_viewed`.
- Transactions: `transaction_created`, `transaction_updated`,
  `transaction_deleted`, `transaction_save_failed`.
- Dashboard: `dashboard_summary_viewed`, `dashboard_period_changed`.
- Search and filters: `search_performed`, `filter_applied`.
- Budgets and categories: `budget_created`, `budget_updated`,
  `category_selected`.
- Sync and background readiness: `sync_started`, `sync_completed`,
  `sync_failed`.
- AI features: `ai_feature_used`.
- Security and privacy: `security_setting_changed`,
  `app_lock_authentication_completed`.
- Error recovery: `error_recovered`.
- Debug and distribution diagnostics: `debug_health_opened`,
  `internal_distribution_readiness_viewed`.

Not every event is logged today. Add logging only in focused follow-up work
where the product value and privacy review are clear.

## Approved Parameters

Approved parameter keys are:

- `screen_name`
- `source`
- `result`
- `error_type`
- `item_type`
- `count_bucket`
- `amount_bucket`
- `currency_present`
- `is_recurring`
- `has_attachment`
- `build_type`
- `app_version`
- `feature_flag_state`

Parameter values should come from typed enums or constrained value classes. Use
buckets and booleans rather than raw values. Examples:

- `amount_bucket=low|medium|high|not_provided`
- `count_bucket=0|1|2_5|6_20|21_plus`
- `currency_present=true|false`
- `result=success|failure|cancelled|empty|unavailable`
- `error_type=validation|database|authentication_unavailable|unknown`

## Forbidden Data

Never add event names or parameters containing:

- Exact transaction amounts, balances, or budgets.
- Merchant names, account names, user-created category names, tags, or notes.
- Search text, raw filters, import/export content, or raw AI prompts/results.
- Raw database IDs, sync payloads, API response bodies, exception messages, or
  stack traces.
- Emails, device identifiers, advertising identifiers, Firebase app IDs, tokens,
  service-account content, keystore data, credentials, or CI secret values.

Redaction is not a substitute for correct taxonomy design. Unsafe data should
not be representable in typed event constructors.

## Adding A New Event

1. Add a stable lower snake case value to `ProductEventName`.
2. Add a typed `ProductEvent` data class or object that exposes only approved
   parameters.
3. Reuse existing enums such as `ProductScreen`, `EventSource`, `EventResult`,
   `ErrorType`, `ItemType`, `CountBucket`, and `AmountBucket` where possible.
4. Add a new enum value only when a generic value is insufficient.
5. Add unit tests in `:core:analytics` for mapping and privacy safety.
6. Update this document if the event category, event name, or parameter key is
   new.
7. Do not wire provider tracking broadly as part of taxonomy-only changes.

## Release Readiness Support

The taxonomy supports E-17 and E-18 release hardening by giving CI/CD,
observability, app health, and future crash triage a consistent vocabulary.
Debug Health reports whether the taxonomy is configured and which provider
behavior is active, but it does not display runtime event payloads through UI,
accessibility labels, crash metadata, or release diagnostics.
## E-12 AI privacy note

AI feature analytics must remain provider-safe. Do not add transaction descriptions, merchant names, account names, category names, notes, search text, exact amounts, exact balances, raw prompts, provider errors, stack traces, or raw IDs to AI events. Use only approved typed event names, result values, feature-flag state, buckets, and generic item types.
