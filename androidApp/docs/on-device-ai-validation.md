# On-device AI runtime validation

Use this pass for Issue #8 on a physical device supported by the ML Kit GenAI
Prompt API. The device must have a locked bootloader, current Google Play
system components, and a compatible AICore/Gemini Nano model.

## Automated gate

```powershell
.\gradlew.bat :core:ai:testDebugUnitTest `
  :feature:search:testDebugUnitTest `
  :feature:transaction:testDebugUnitTest `
  :app:assembleDebug `
  :app:lintDebug `
  --console=plain
```

## Supported-device pass

1. Install the debug APK and wait for AICore initialization/model download to
   finish.
2. Add at least 20 transactions with overlapping merchant, note, category,
   recurring, and amount patterns.
3. In Search, select Semantic and query by intent rather than exact keywords.
   Confirm the accessibility description reports `on-device AI`.
4. Repeat with category/date/type filters and confirm every result still
   satisfies the selected filters.
5. In Transaction Editor, enter a merchant or note, request Smart autofill,
   and verify every proposed category belongs to the current transaction type.
6. Pre-fill amount, category, or note before requesting autofill and confirm
   accepting the suggestion does not replace those explicit values.
7. Disable each AI feature flag and confirm the app remains usable.
8. Remove/disable AICore support or repeat on an unsupported device. Confirm
   Search reports `local fallback`, summaries remain rule-based, and Smart
   autofill either produces a local suggestion or a non-blocking no-suggestion
   message.
9. Exercise airplane mode after the model is available and confirm inference
   remains local.

## Evidence record

| Field | Value |
| --- | --- |
| Commit | |
| Device/model | |
| Android build | |
| AICore/Gemini Nano version | |
| ML Kit Prompt API | `1.0.0-beta2` |
| Model status before test | Available / Downloadable / Downloading / Unsupported |
| Semantic search result | Pass / Fail |
| Smart autofill result | Pass / Fail |
| Timeout/fallback result | Pass / Fail |
| Airplane-mode result | Pass / Fail |
| Tester/date | |

An emulator without compatible AICore can validate the fallback path, but it
cannot provide evidence for real Gemini Nano inference.
