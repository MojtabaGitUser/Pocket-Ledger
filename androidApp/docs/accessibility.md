# Pocket Ledger Accessibility Semantics

Pocket Ledger screens must expose a clear semantic structure for TalkBack,
accessibility services, and Compose UI tests. Accessibility work should improve
the existing UI structure without changing feature behavior or adding hidden
data that is not already visible.

## Screen And Section Headings

Primary screen titles should be marked as headings. Section titles should also
be headings, including dashboard sections, form sections, and placeholder
screen titles.

Use the design-system helper when possible:

```kotlin
Text(
    text = "Transactions",
    modifier = Modifier.pocketLedgerHeading(),
)
```

`SectionHeader` marks its title as a heading by default.

## Navigation

Top-level navigation items must expose:
- A stable label or content description.
- Selected state through Compose selected semantics.
- A spoken state description such as `Selected` or `Not selected`.

Navigation icons that are represented by short text should not be the only
discoverable label. The full destination name must remain available to
accessibility services.

## Icon Buttons And Actions

Icon-only or compact actions need explicit labels. Text actions may rely on
visible text, but critical actions should also have stable descriptions when UI
tests or TalkBack need an unambiguous target.

Examples:
- `Edit transaction`
- `Delete transaction`
- `Confirm delete transaction`
- `Cancel delete transaction`
- `Clear search filters`

Clickable transaction rows should expose an action label such as
`Open transaction details`.

## Transaction Rows

Transaction rows should be announced as one coherent item. The row description
should include the visible critical fields:
- Merchant or fallback title.
- Income/expense type.
- Category when available.
- Date.
- Note preview when visible.
- Amount.
- Tags when visible.

Do not add hidden transaction details to the semantics tree. If a full note or
merchant is not visible, do not expose more than the visible preview.

Selected rows in adaptive list/detail mode must expose selected state and a
state description.

## Filter Chips And Toggle State

Filter chips, category chips, tag chips, transaction type choices, recurring
flags, and budget active switches must expose selected or checked state.

Use state descriptions for spoken clarity:
- `Selected` / `Not selected`
- `On` / `Off`

Search filters should keep distinct labels for type, category, tag, date, and
amount filters so tests and accessibility services can identify them.

## Semantic Labels And State Descriptions

Use semantic labels and state descriptions where they make TalkBack output more
useful than the visible text alone.

Checklist:

- Icon-only and compact actions have meaningful labels, for example
  `Edit transaction`, `Delete transaction`, `Clear search filters`, or
  `Unlock Pocket Ledger`.
- Decorative navigation glyphs and display-only tag/status chips do not expose
  fake actions or noisy duplicate labels.
- Interactive controls expose their role through the platform component or an
  explicit role when needed, and expose selected, checked, enabled, disabled,
  loading, error, empty, active, inactive, unavailable, or authenticating state
  where applicable.
- Loading, empty, unavailable, and error states provide a combined spoken
  description and a state description such as `Loading`, `Empty`, or `Error`.
- Financial summaries, budget progress, category totals, and transaction rows
  use spoken descriptions that include only visible, relevant information and
  are understandable when read aloud.
- Debug or app-health UI uses diagnostic-safe labels and states. Do not expose
  stack traces, internal IDs, raw enum names, secrets, or sensitive release
  diagnostics through accessibility text.
- TalkBack navigation order follows the visible screen structure: screen title,
  primary controls, filters/forms, content, and retry/confirmation actions.
- Compose tests cover critical semantics for navigation state, loading/error
  states, transaction rows, form fields, chips/toggles, and app-lock states.

## Progress And Metrics

Dashboard metric cards should expose readable descriptions that include the
metric label and amount.

Progress indicators should expose a state description, for example:
- `67% of expenses`
- `90% budget progress, Near limit`

Keep the visible progress text and spoken progress text consistent.

## Empty, Loading, And Error States

Loading states should use polite live regions and describe what is loading.
Empty and error states should expose a combined title and message. Retry actions
must remain labeled.

Examples:
- `Loading transactions`
- `No matching transactions. Clear filters or try a different keyword.`
- `Could not load dashboard. Retry`

## Tests

Compose UI tests should assert semantics for primary paths, not only visible
text. Preferred coverage includes:
- Navigation labels and selected state descriptions.
- Dashboard section headings and progress descriptions.
- Transaction row content descriptions and selected state.
- Transaction detail edit/delete action labels.
- Search input, filter selected state, and clear-filter action.
- Budget setup field labels, category selected state, and active switch state.
