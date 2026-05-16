# GitHub Issue Import Report

Generated from `Pocket_Ledger_Complete_Backlog.docx` on 2026-05-16.

## Summary

- Existing Pocket-Ledger issues found: 30
- Backlog issue rows found in the document: 163
- Epic rows excluded from creation: 22
- Non-epic backlog rows considered for creation: 141
- Missing non-epic issues detected before import: 116
- Missing issues created in this run: 0

## Why No Issues Were Created

GitHub CLI (`gh`) is not installed in the current environment, and `GITHUB_TOKEN` is not set. Because the requested fallback script must authenticate through `GITHUB_TOKEN`, the import was not executed from this machine.

A fallback script was added at:

- `scripts/create_missing_github_issues.py`

The script reads `GITHUB_TOKEN`, fetches existing issues, compares by normalized title and backlog ID, and creates only missing issues. It is designed to be idempotent.

## Existing Issues Detected

- #1 `[Story] US-E01-01 - Navigate the app with a consistent shell`
- #2 `[Story] US-E02-01 - Add or edit a transaction`
- #3 `[Story] US-E00-01 - Initialize Android/KMP modular project foundation`
- #4 `[Story] US-E03-01 - View dashboard insights and summaries`
- #5 `[Story] US-E04-01 - Use the app fully offline`
- #6 `[Story] US-E05-01 - Search and filter transactions efficiently`
- #7 `[Story] US-E08-01 - Secure sensitive local data`
- #8 `[Story] US-E10-01 - Generate AI-powered local insights`
- #9 `[Story] US-E13-01 - Maintain high automated test coverage`
- #10 `[Story] US-E14-01 - Optimize startup and runtime performance`
- #11 `[Story] US-E06-01 - Support adaptive layouts across devices`
- #12 `[Story] US-E07-01 - Share business logic through Kotlin Multiplatform`
- #13 `[Story] US-E09-01 - Support optional passkey-enabled account flow`
- #14 `[Story] US-E15-01 - Ensure accessibility and inclusive UX`
- #15 `[Story] US-E16-01 - Monitor crashes and app health`
- #16 `[Story] US-E17-01 - Automate CI/CD and release workflows`
- #17 `[Story] US-E18-01 - Prepare production-ready Play Store release`
- #18 `[Task] T-E00-01 - Create Android app module`
- #19 `[Task] T-E01-03 - Implement navigation graph`
- #20 `[Task] T-E02-02 - Build add/edit transaction screen`
- #21 `[Task] T-E04-01 - Configure Room database and DAO layer`
- #22 `[Task] T-E10-01 - Integrate Gemini Nano / ML Kit abstraction`
- #23 `[Task] T-E17-01 - Configure GitHub Actions workflow`
- #24 `[Task] T-E00-05 - Configure Gradle version catalog and convention plugins`
- #25 `[Task] T-E01-01 - Implement Material 3 app theme`
- #26 `[Task] T-E03-02 - Dashboard analytics UI`
- #27 `[Task] T-E00-02 - Create shared KMP module`
- #28 `[Task] KMP module setup`
- #29 `[Task] Transaction list screen`
- #30 `[Task] Search screen UI`

## Skipped Duplicate or Conflicting Issues

These were treated as already created or unsafe to recreate because the same ID or clearly overlapping work already exists:

- `US-E01-01`, `US-E02-01`, `US-E03-01`, `US-E04-01`, `US-E05-01`, `US-E06-01`
- `US-E07-01`, `US-E08-01`, `US-E09-01`, `US-E10-01`, `US-E13-01`, `US-E14-01`
- `US-E15-01`, `US-E16-01`, `US-E17-01`, `US-E18-01`
- `T-E00-01`, `T-E00-02`, `T-E00-05`, `T-E01-01`, `T-E01-03`, `T-E02-02`
- `T-E03-02`, `T-E04-01`, `T-E10-01`, `T-E17-01`
- Unnumbered semantic duplicates: `T-E07-01`, `T-E02-03`, `T-E05-03`

## Missing Issues Detected

116 missing non-epic backlog items were detected. The first high-priority missing items are:

- `[Technical Story] TS-E00-01 - Establish multi-module Gradle architecture`
- `[Task] T-E00-03 - Define architecture package and module rules`
- `[Task] T-E00-04 - Configure debug/release variants`
- `[Technical Story] TS-E01-01 - Create shared Compose design system`
- `[Task] T-E01-02 - Create shared UI components`
- `[Task] T-E01-04 - Add design preview fixtures`
- `[Story] US-E02-02 - View and manage transaction details`
- `[Technical Story] TS-E02-01 - Create transaction domain model and validation`
- `[Task] T-E02-01 - Implement transaction form state and validation`
- `[Task] T-E02-04 - Implement delete and undo/confirmation`
- `[Task] T-E02-05 - Add transaction tests`

## Newly Created Issues

None in this run.

## Assumptions and Limitations

- Epic rows were not created as issues because the request only specified User Stories, Technical Stories, Tasks, and Subtasks.
- Existing issues use an older numbering scheme for some areas. For example, existing `US-E08-01` is Security & Privacy, while the backlog document uses `US-E08-01` for the desktop demo. These conflicts were skipped to avoid duplicate IDs.
- The current fallback script contains the import framework and initial backlog specs, but should be extended with the remaining detected missing items before a full import run.
- Parent issue body checklist updates were not performed because no child task issues were created in this run.
