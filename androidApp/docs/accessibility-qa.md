# Accessibility QA Pass

This document completes the manual QA artifact for #14, `US-E15-01 - Ensure accessibility and inclusive UX`.

Use it before closing accessibility work, before releasing UI-heavy changes, and before Play Store promotion. Automated Compose, Paparazzi, and lint checks protect important regressions, but they do not replace screen-reader and keyboard traversal review on a real or emulator-backed Android runtime.

## Issue #14 Acceptance Map

| Acceptance criterion | Current evidence | QA gate |
| --- | --- | --- |
| TalkBack labels and semantics exist for interactive UI. | Compose semantics, `contentDescription`, `stateDescription`, selected/checked state, headings, and design-system semantics helpers are documented in `androidApp/docs/accessibility.md` and used across app shell, dashboard, transactions, search, budget setup, settings, and app lock. | Run the TalkBack pass below for changed screens and release candidates. |
| App supports 200% font scaling. | `TwoHundredPercentFontScaleScreenshotTest`, committed 200% baselines, and `androidApp/docs/accessibility-200-font-scale.md`. | Run focused or full Paparazzi verification after UI/font-scale changes. |
| Color contrast follows accessibility recommendations. | Material theme tokens and PR accessibility checklist require contrast/readability review. | Review light/dark snapshots and changed colors; color must not be the only signal. |
| Important screens are keyboard and screen-reader navigable. | Navigation and controls expose labels/states; app-shell and feature tests assert semantics where feasible. | Run TalkBack linear navigation and keyboard/D-pad traversal below. |
| Accessibility checks are part of QA. | `.github/pull_request_template.md`, this document, `androidApp/docs/testing.md`, and `docs/release/release-checklist.md`. | UI PRs and release candidates must reference this pass or explain why it is N/A. |

## Required Screens

Review these primary screens for #14 and release candidates:

- Dashboard
- Transactions list and adaptive list/detail
- Transaction detail
- Transaction editor
- Search with filters and populated results
- Budget setup
- Settings
- App lock locked/authentication state
- Empty, loading, unavailable, and error states when changed
- Debug Health when debug/app-health UI changes; it must remain absent from release navigation

## TalkBack Pass

Use a debug or release-like build with deterministic sample data only. Do not run this pass with personal financial data.

For a primary-screen audit, use the repository runner from the project root. It refuses to start
unless the requested package is installed and TalkBack is enabled, captures a screenshot and UI
hierarchy per screen, and writes both machine-readable JSON and a reviewer-signed Markdown record.

```powershell
.\scripts\run_accessibility_audit.ps1 `
  -DeviceSerial <adb-serial> `
  -AndroidUserId 0 `
  -PackageName com.mojtaba.folentra `
  -BuildVariant benchmark `
  -LayoutDeviceClass Compact `
  -ApkPath .\androidApp\app\build\outputs\apk\benchmark\app-benchmark.apk `
  -Reviewer "<reviewer-name>"
```

Use `com.mojtaba.folentra.debug` when auditing a debug build. The generated evidence is intentionally
gitignored because screenshots and device metadata can be sensitive. Copy only the generated
`qa-record.md` table into the issue or PR after reviewing and removing any sensitive notes.
Omit `-ApkPath` only when intentionally auditing a preinstalled Play build. The runner never
uninstalls a differently signed package or deletes app data.

1. Enable TalkBack on the device or emulator.
2. Open Folentra and start from the first visible destination.
3. Swipe forward through the whole screen.
4. Swipe backward through the same screen.
5. Activate every primary action with double tap.
6. Enter and edit text fields where the screen has forms.
7. Verify dialogs, snackbars, validation errors, empty states, loading states, and error states are announced clearly.
8. Rotate or resize where relevant for adaptive states.
9. Repeat on compact phone and expanded/tablet style layout when the screen has adaptive behavior.

Pass criteria:

- Screen title or first meaningful heading is announced early.
- Navigation items announce destination and selected state.
- Buttons, icon buttons, switches, chips, filters, rows, and retry/save actions have meaningful labels.
- Selected, checked, disabled, loading, error, empty, unavailable, authenticating, and locked states have understandable spoken state.
- Transaction rows announce visible merchant/title, type, category/date when visible, amount, and visible tags without exposing hidden private details.
- Financial summaries and progress components are understandable when read aloud.
- Focus order follows visible structure: title, primary controls, filters/forms, content, secondary actions.
- Primary actions are reachable and operable through TalkBack.
- Text fields expose useful labels and validation errors.
- Decorative icons do not create noisy duplicate stops.
- Debug or diagnostics UI does not expose stack traces, credentials, internal IDs, Firebase tokens, tester emails, or personal financial data.

## Keyboard And D-Pad Pass

Run this with a hardware keyboard, emulator keyboard, or D-pad capable emulator profile.

1. Move focus forward with Tab or D-pad next.
2. Move focus backward with Shift+Tab where available.
3. Activate focused controls with Enter, Space, or D-pad center.
4. Use arrow keys or D-pad movement for navigation containers and selectable rows.
5. Confirm scrolling content remains reachable when focus moves past the visible viewport.

Pass criteria:

- Focus does not get trapped in navigation, dialogs, lists, or forms.
- Important controls have visible focus indication from the platform/component.
- Focus order is close to reading order and visual order.
- Save, retry, clear filters, unlock, navigation, and settings toggles are reachable.
- Text fields can be focused, edited, and left without losing entered state.

## 200% Font-Scale Pass

The automated #117 coverage is the baseline. For UI changes, also review the changed screen manually with Android font size set to 200%.

Pass criteria:

- Important text is not clipped, overlapped, or hidden.
- Primary actions remain reachable.
- Long financial text reflows or stacks instead of colliding with adjacent content.
- Dialogs, forms, and validation messages remain scrollable when needed.
- TalkBack labels and state descriptions still match the visible UI after reflow.

Focused validation command:

```powershell
.\androidApp\gradlew.bat :app:verifyPaparazziDebug --tests "com.mojtaba.folentra.screenshot.TwoHundredPercentFontScaleScreenshotTest" --console=plain
```

Full screenshot validation command:

```powershell
.\androidApp\gradlew.bat verifyAdaptiveScreenshots --console=plain
```

## Color And Contrast Pass

Review changed screens in light and dark theme.

Pass criteria:

- Body text, labels, transaction amounts, validation errors, and secondary text remain readable.
- Status, warning, error, income, expense, selected, disabled, and progress states do not rely on color alone.
- Icons that convey meaning have text labels, semantic labels, or adjacent text.
- Disabled controls still communicate disabled state through platform semantics or state description.

## Automated implementation record — 2026-07-26

The code-assisted portion of the #14 / #134 accessibility pass now includes:

- Polite live-region announcements for visible validation errors in Transaction Editor and Budget Setup.
- Instrumentation assertions that validation errors expose both error semantics and `LiveRegionMode.Polite`.
- 200% font-scale Paparazzi coverage for Transaction Editor in valid and validation-error states on compact-phone and expanded-tablet layouts.
- Existing primary-screen semantics, adaptive screenshots, and manual QA instructions remain the baseline for the full pass.

Automated verification:

| Check | Result |
| --- | --- |
| App screenshot test compilation | Pass |
| Dashboard instrumentation test compilation | Pass |
| Transaction instrumentation test compilation | Pass |
| Transaction Editor 200% Paparazzi verification | Pass |

The automated result does not replace listening to TalkBack output, observing hardware-keyboard/D-pad focus movement, or human contrast review. Those checks remain pending until recorded against a named device/build in the template below or by `scripts/run_accessibility_audit.ps1`.

## Primary-screen semantics audit (#115)

The code-level audit covers Dashboard, Transactions list/detail/editor, Search, Budget setup,
Settings, and App Lock. Compose tests assert headings, labels, selected/checked/error/loading states,
and primary actions. Settings switch rows use one switch-role semantics node for the complete row;
their decorative trailing switches are hidden from the semantics tree to prevent duplicate TalkBack
stops while preserving row-wide activation.

The runtime portion is deliberately reviewer-attested. A screenshot or UI hierarchy cannot prove
what TalkBack spoke, so #115 must not be marked runtime-Pass until the runner produces an all-Pass
record from a named reviewer, device, Android/TalkBack version, package, build variant, and commit.

## QA Record Template

Copy this table into the PR description, release record, or issue comment when #14-related QA is run.

| Field | Value |
| --- | --- |
| Build variant and commit |  |
| Device/emulator and Android version |  |
| TalkBack version |  |
| Font scale |  |
| Layout/device class | Compact / Medium / Expanded |
| Screens covered |  |
| Automated checks run |  |
| Result | Pass / Fail / N/A |
| Follow-up issues |  |
| Reviewer |  |
| Date |  |

## Closure Checklist For #14

- [x] Compose semantics and content descriptions are present on primary interactive UI.
- [x] Navigation, rows, forms, chips, switches, loading/error/empty states, and app lock expose useful labels or state descriptions.
- [x] 200% font-scale Paparazzi validation passes and baselines are committed.
- [ ] TalkBack pass is run or explicitly marked N/A for non-UI changes.
- [ ] Keyboard/D-pad traversal is run or explicitly marked N/A for non-UI changes.
- [ ] Light/dark contrast/readability is reviewed for changed screens.
- [ ] PR template accessibility checklist is completed for UI changes.
- [ ] Release checklist links this QA pass before Play Store promotion.

#14 is closeable when this checklist is satisfied for the current implemented primary screens and any remaining manual review notes are recorded in the PR, release record, or issue comment.
