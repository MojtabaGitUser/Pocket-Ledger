# 200% Font Scale Validation

This document records the validation evidence for #117, `T-E16-03 - Test 200% font scaling`.

Pocket Ledger treats 200% font scale as a release-significant accessibility gate. The goal is not pixel-perfect density parity; the goal is that primary screens stay understandable, important content does not overlap or clip, and primary actions remain reachable when Android font size is set to 200%.

## Automated Coverage

The focused 200% coverage lives in:

- `androidApp/app/src/test/java/com/mojtaba/pocketledger/screenshot/TwoHundredPercentFontScaleScreenshotTest.kt`
- `androidApp/app/src/test/java/com/mojtaba/pocketledger/screenshot/AdaptiveDeviceMatrix.kt`
- `androidApp/app/src/test/snapshots/images/`

The test uses `AdaptiveDeviceMatrix.TwoHundredPercentFontScaleDevices`, which renders compact phone and expanded tablet configurations with `fontScale = 2.0f`.

Covered screens and states:

| Area | 200% scenario | What the snapshot protects |
| --- | --- | --- |
| Dashboard | Summary content | Financial summary, metric cards, insight/category sections, and reflow at large text. |
| Transactions | Adaptive list/detail content | Transaction rows, selected detail content, amount text, and list/detail layout resilience. |
| Search | Populated results | Search field, filters/results composition, transaction result descriptions, and large text readability. |
| Budget setup | Valid form content | Form labels, amount/category controls, validation surface, and scrollable setup layout. |
| Settings | App-lock available state | Settings headings, switch row, state text, and reachable app-lock control. |
| App lock | Locked state | Lock heading/message, authentication action, state text, and large text fit. |

This coverage complements the broader adaptive screenshot suite at normal, 130%, and 150% font scales. It is intentionally focused so #117 protects the highest-risk primary paths without turning every screenshot variant into a slow 200% matrix.

## Validation Command

From the repository root on Windows:

```powershell
.\androidApp\gradlew.bat :app:verifyPaparazziDebug --tests "com.mojtaba.pocketledger.screenshot.TwoHundredPercentFontScaleScreenshotTest" --console=plain
```

From `androidApp/` on macOS/Linux:

```bash
./gradlew :app:verifyPaparazziDebug --tests "com.mojtaba.pocketledger.screenshot.TwoHundredPercentFontScaleScreenshotTest" --console=plain
```

Run the full screenshot suite when a UI change may affect surrounding adaptive states:

```powershell
.\androidApp\gradlew.bat verifyAdaptiveScreenshots --console=plain
```

If a deliberate UI change updates the expected 200% layout, record new baselines with:

```powershell
.\androidApp\gradlew.bat :app:recordPaparazziDebug --tests "com.mojtaba.pocketledger.screenshot.TwoHundredPercentFontScaleScreenshotTest" --console=plain
```

Review generated diffs before committing new snapshots. Do not accept a baseline that clips important text, overlaps controls, hides primary actions, or makes financial values ambiguous.

## Manual Review Checklist

Use this checklist when reviewing #117-related changes or future large-font UI changes:

- [ ] Dashboard, transactions, search, budget setup, settings, and app lock render at 200% font scale.
- [ ] Titles, form labels, validation errors, transaction amounts, financial summaries, category totals, and action labels remain understandable.
- [ ] Primary actions remain reachable, including save, retry, clear filters, app-lock unlock, and settings toggles.
- [ ] Long financial text reflows or stacks instead of overlapping adjacent content.
- [ ] Scrollable content remains scrollable when large text exceeds the viewport.
- [ ] Accessibility labels and state descriptions still describe the visible UI after layout reflow.
- [ ] Debug or diagnostics UI does not expose secrets, stack traces, internal IDs, or personal financial data through large-font labels.

## Known Limits

Paparazzi validates static Compose rendering, not runtime TalkBack traversal, hardware keyboard traversal, real-device text rasterization, or animation behavior. Those manual screen-reader checks remain part of the broader accessibility story (#14). #117 is complete when the 200% screenshot validation passes and this evidence remains linked from the accessibility and testing docs.
