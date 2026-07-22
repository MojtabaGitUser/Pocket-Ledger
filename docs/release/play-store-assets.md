# Play Store Asset Plan

This document tracks repository-ready Play Store listing assets for #131 and
feeds the parent #17 Play Store release story. It does not claim that assets
were uploaded to Play Console. Final dimensions,
format rules, and policy checks must still be verified in Play Console before
submission.

## Current Repository Assets

| Asset | Repository status | Release note |
| --- | --- | --- |
| App name | `@string/app_name` is `Folentra`. | Store title should use `Folentra` for readability unless product naming changes. |
| Launcher icon | Adaptive and round launcher resources exist under `androidApp/app/src/main/res/mipmap-*` and foreground/background drawables exist under `res/drawable`. | Review final brand artwork before public release; current resources compile. |
| Privacy policy copy | `docs/privacy-policy.md`. | Must be hosted at a public HTTPS URL before Play Store submission. |
| Play Store readiness checklist | `docs/play-store-readiness.md`. | Use it for app content, data safety, permissions, and limitations review. |
| Release checklist | `docs/release/release-checklist.md`. | Final release gate for signing, privacy, store listing, smoke tests, and manual review. |


## #131 Acceptance Matrix

| Requirement | Repository evidence | Status |
| --- | --- | --- |
| Listing copy exists. | Store title, short description, full description draft, and release notes template are maintained in this file. | Complete. |
| App icon source exists. | Launcher adaptive icon XML and foreground/background resources exist under `androidApp/app/src/main/res/`. | Complete for source; high-res PNG export remains manual. |
| Screenshots are planned from real app screens. | Screenshot capture plan covers dashboard, transactions, search, insights, and settings/app lock. | Complete for plan; binary captures remain manual. |
| Feature graphic direction exists. | Required graphic assets table defines a feature graphic using actual app value and sample-safe data. | Complete for direction; final binary graphic remains manual. |
| Store claims avoid unsupported behavior. | Listing copy says local-first, no bank connection, no financial advice, no required account, and no Folentra server sync. | Complete. |
| Play Console handoff remains explicit. | Manual Play Console steps list hosted privacy URL, Data Safety, app-content forms, and signed AAB upload. | Complete. |

## Store Listing Copy

Store title:

```text
Folentra
```

Short description:

```text
Track spending, budgets, search, and local insights in a private ledger.
```

Full description draft:

```text
Folentra is a local-first personal finance ledger for tracking
transactions, budgets, categories, tags, and monthly summaries.

Record income and expenses, review recent activity, search your ledger, and
see private spending insights generated on device or through deterministic
local fallback logic. The current app does not connect to banks, provide
financial advice, require an account, or sync ledger records to a Folentra
server.

Folentra is designed for users who want a simple private ledger they
control. Ledger records stay in app-private storage on the device. Android
backup and device-transfer rules exclude the ledger database by default. A
local-first backup-ready profile foundation exists, but encrypted backup and
restore are not implemented.
```

Release notes template:

```text
Initial release candidate:
- Local transaction, budget, category, and tag tracking.
- Dashboard summaries, search, and private local insights.
- Optional app lock using Android system authentication.
- Privacy-safe Android backup and device-transfer defaults.
```

## Required Graphic Assets

Create final graphic files outside this repository or in a future reviewed
asset commit. Use sample-safe ledger data only in screenshots.

| Asset | Expected Play Console requirement to verify | Source screen | Suggested repo location if committed | Acceptance criteria |
| --- | --- | --- | --- | --- |
| High-res app icon | 512 x 512 PNG, no transparency unless Play Console guidance allows it. | Launcher artwork. | `docs/release/assets/play-store/icon-512.png` | Matches app launcher brand, readable at small sizes, no private data. |
| Feature graphic | 1024 x 500 PNG or JPG. | Branded composition using dashboard/search/insights screenshots. | `docs/release/assets/play-store/feature-graphic.png` | Shows actual app value, no sensitive data, no unsupported claims. |
| Phone screenshots | Current Play Console phone screenshot dimensions and count. | Dashboard, transactions, search, insights, settings/app lock. | `docs/release/assets/play-store/phone/` | Uses deterministic demo data, readable text, no debug UI, no private ledger data. |
| Tablet screenshots | Current Play Console tablet screenshot dimensions and count if tablet listing is used. | Adaptive dashboard/search/insights layouts. | `docs/release/assets/play-store/tablet/` | Demonstrates adaptive UI and avoids cropped or overlapping content. |
| Privacy policy URL | Public HTTPS URL. | Hosted `docs/privacy-policy.md`. | Not stored as binary asset. | URL resolves publicly and matches submitted app behavior. |

## Screenshot Capture Plan

Capture screenshots from a release or release-like build, not debug UI:

1. Dashboard with deterministic sample data.
2. Transaction list with sample income and expenses.
3. Search with a sample-safe query and results.
4. Insights with local/rule-based provider status visible.
5. Settings showing optional app lock without exposing debug diagnostics.

Screenshots must not include real merchant names, notes, account names, exact
balances from a real user, tester emails, debug health, stack traces, build
secrets, Firebase IDs, or Play Console credentials.

## Manual Play Console Steps

- Upload final graphic assets.
- Enter the hosted privacy policy URL and public support contact.
- Complete Data Safety based on `docs/play-store-readiness.md`.
- Complete app access, ads, content rating, financial features, target audience,
  and permissions declarations using the final release artifact.
- Upload the signed release AAB only after the final release checklist passes.

## Traceability

- #131: repository listing copy, asset acceptance matrix, screenshot plan, and
  manual Play Console handoff steps are prepared; binary graphics and Play
  Console upload remain manual.
- #128: release-ready install evidence is tracked in
  `docs/release/release-ready-install.md` and `docs/release/smoke-test.md`.
- #129: final release gate references this asset plan.
- #132: privacy policy copy is maintained separately in `docs/privacy-policy.md`.
- #17: parent Play Store release story depends on this file plus signing,
  release-ready install evidence, hosted privacy policy, app-content forms,
  and final release checklist review.
