# GitHub Issue Import Report

Generated from `Pocket_Ledger_Complete_Backlog.docx` on 2026-05-16.

## Summary

- Existing Pocket-Ledger issues found: 30
- Backlog issue rows found in the document: 163
- Epic rows excluded from creation: 22
- Non-epic backlog rows considered for creation: 141
- Missing non-epic issues detected: 116
- Missing issues created in this update: 0

No GitHub issues were created in this update. This pass only completed the importer script and report.

## Script Status

`scripts/create_missing_github_issues.py` now contains the complete embedded `BACKLOG_ISSUES` list with all 116 missing non-epic backlog issues detected from the DOCX.

The script includes a runtime assertion:

```python
assert len(BACKLOG_ISSUES) == 116
```

The script is idempotent: it fetches existing issues, compares by normalized title and backlog ID, creates only missing issues, and updates parent Story / Tech Story bodies with linked child Task checklists after creation.

## How To Run Locally

From the repository root:

```powershell
$env:GITHUB_TOKEN = "YOUR_GITHUB_TOKEN_WITH_REPO_ISSUE_ACCESS"
python scripts/create_missing_github_issues.py
```

On macOS/Linux:

```bash
export GITHUB_TOKEN="YOUR_GITHUB_TOKEN_WITH_REPO_ISSUE_ACCESS"
python3 scripts/create_missing_github_issues.py
```

Required token permissions:

- Repository access to `MojtabaGitUser/Pocket-Ledger`
- Permission to read issues and milestones
- Permission to create and edit issues

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

## Full List Of Missing Issues Embedded In Script

1. `[Technical Story] TS-E00-01 - Establish multi-module Gradle architecture`
2. `[Task] T-E00-03 - Define architecture package and module rules`
3. `[Task] T-E00-04 - Configure debug/release variants`
4. `[Technical Story] TS-E01-01 - Create shared Compose design system`
5. `[Task] T-E01-02 - Create shared UI components`
6. `[Task] T-E01-04 - Add design preview fixtures`
7. `[Story] US-E02-02 - View and manage transaction details`
8. `[Technical Story] TS-E02-01 - Create transaction domain model and validation`
9. `[Task] T-E02-01 - Implement transaction form state and validation`
10. `[Task] T-E02-04 - Implement delete and undo/confirmation`
11. `[Task] T-E02-05 - Add transaction tests`
12. `[Story] US-E03-02 - Set and view simple budget status`
13. `[Technical Story] TS-E03-01 - Implement aggregation and budget status use cases`
14. `[Task] T-E03-01 - Create dashboard summary models`
15. `[Task] T-E03-03 - Build Dashboard Compose screen`
16. `[Task] T-E03-04 - Implement simple budget setup`
17. `[Task] T-E03-05 - Add dashboard tests`
18. `[Technical Story] TS-E04-01 - Implement Room KMP local database`
19. `[Task] T-E04-02 - Implement DAOs and repositories`
20. `[Task] T-E04-03 - Add database migrations and tests`
21. `[Task] T-E04-04 - Add seed/demo data tools`
22. `[Task] T-E04-05 - Create offline-first repository contract`
23. `[Technical Story] TS-E05-01 - Create shared search model and ranking rules`
24. `[Task] T-E05-01 - Define SearchQuery and filter models`
25. `[Task] T-E05-02 - Implement indexed keyword search`
26. `[Task] T-E05-04 - Connect search to semantic mode placeholder`
27. `[Technical Story] TS-E06-01 - Create adaptive layout infrastructure`
28. `[Task] T-E06-01 - Create adaptive navigation shell`
29. `[Task] T-E06-02 - Implement transaction list/detail adaptive layout`
30. `[Task] T-E06-03 - Adapt dashboard layout`
31. `[Task] T-E06-04 - Add adaptive screenshot test matrix`
32. `[Technical Story] TS-E07-01 - Move stable business logic to shared KMP`
33. `[Task] T-E07-02 - Move domain models and validation`
34. `[Task] T-E07-03 - Share search and aggregation rules`
35. `[Task] T-E07-04 - Document KMP boundaries`
36. `[Technical Story] TS-E08-01 - Create desktop target consuming shared logic`
37. `[Task] T-E08-01 - Add desktop demo module`
38. `[Task] T-E08-02 - Implement desktop Search screen`
39. `[Task] T-E08-03 - Implement desktop Insights screen`
40. `[Task] T-E08-04 - Document desktop demo limitations`
41. `[Technical Story] TS-E09-01 - Create background job infrastructure`
42. `[Task] T-E09-01 - Create WorkManager scheduler abstraction`
43. `[Task] T-E09-02 - Implement monthly summary preparation worker`
44. `[Task] T-E09-03 - Implement reminder scheduling settings`
45. `[Task] T-E09-04 - Expose worker status in Debug Health`
46. `[Technical Story] TS-E10-01 - Implement privacy-safe local security layer`
47. `[Task] T-E10-02 - Implement encrypted sensitive preferences`
48. `[Task] T-E10-03 - Implement optional app lock`
49. `[Task] T-E10-04 - Add privacy-safe logging policy`
50. `[Task] T-E10-05 - Document security model and limitations`
51. `[Story] US-E11-01 - Create an optional backup-ready profile`
52. `[Technical Story] TS-E11-01 - Define optional passkey backend contract`
53. `[Task] T-E11-01 - Add optional account settings entry`
54. `[Task] T-E11-02 - Define passkey API contract`
55. `[Task] T-E11-03 - Implement Credential Manager prototype client`
56. `[Task] T-E11-04 - Add Play Integrity request hook`
57. `[Story] US-E12-01 - Generate a private monthly summary`
58. `[Story] US-E12-02 - Use semantic search and smart autofill`
59. `[Technical Story] TS-E12-01 - Create AI provider abstraction with fallback`
60. `[Task] T-E12-01 - Define AI feature contracts`
61. `[Task] T-E12-02 - Implement rule-based fallback provider`
62. `[Task] T-E12-03 - Integrate on-device AI provider`
63. `[Task] T-E12-04 - Build Insights screen`
64. `[Task] T-E12-05 - Add AI privacy and safety tests/checklist`
65. `[Technical Story] TS-E13-01 - Implement feature flag provider abstraction`
66. `[Task] T-E13-01 - Define typed feature flags`
67. `[Task] T-E13-02 - Implement local JSON/default provider`
68. `[Task] T-E13-03 - Add debug flag override screen`
69. `[Task] T-E13-04 - Document Play Feature Delivery strategy`
70. `[Technical Story] TS-E14-01 - Implement layered testing framework`
71. `[Task] T-E14-01 - Create core-testing module`
72. `[Task] T-E14-02 - Add shared unit tests`
73. `[Task] T-E14-03 - Add Room and repository integration tests`
74. `[Task] T-E14-04 - Add Compose UI tests for critical flows`
75. `[Task] T-E14-05 - Add screenshot test matrix`
76. `[Task] T-E14-06 - Create testing report`
77. `[Technical Story] TS-E15-01 - Benchmark startup and scrolling performance`
78. `[Task] T-E15-01 - Set up Macrobenchmark module`
79. `[Task] T-E15-02 - Set up Baseline Profile generation`
80. `[Task] T-E15-03 - Add large dataset performance scenario`
81. `[Task] T-E15-04 - Run recomposition and jank review`
82. `[Task] T-E15-05 - Tune release build and R8`
83. `[Task] T-E15-06 - Add LeakCanary/profiler pass in debug`
84. `[Technical Story] TS-E16-01 - Add accessibility testing and checklist`
85. `[Task] T-E16-01 - Audit semantics for primary screens`
86. `[Task] T-E16-02 - Add semantic labels and state descriptions`
87. `[Task] T-E16-03 - Test 200% font scaling`
88. `[Task] T-E16-04 - Add accessibility checks to PR template`
89. `[Technical Story] TS-E17-01 - Implement privacy-safe observability`
90. `[Task] T-E17-02 - Configure App Distribution/internal tester flow`
91. `[Task] T-E17-03 - Implement Debug Health screen`
92. `[Task] T-E17-04 - Define product event taxonomy`
93. `[Technical Story] TS-E18-01 - Create GitHub Actions delivery pipeline`
94. `[Task] T-E18-01 - Add PR validation workflow`
95. `[Task] T-E18-02 - Add release candidate workflow`
96. `[Task] T-E18-03 - Add screenshot/benchmark workflow strategy`
97. `[Task] T-E18-04 - Publish CI badges and commands`
98. `[Story] US-E19-01 - Install a release-ready Pocket Ledger build`
99. `[Technical Story] TS-E19-01 - Create release hardening checklist`
100. `[Task] T-E19-01 - Create release signing and versioning plan`
101. `[Task] T-E19-02 - Prepare Play Store assets`
102. `[Task] T-E19-03 - Write privacy policy`
103. `[Task] T-E19-04 - Complete app content checklist`
104. `[Task] T-E19-05 - Run release candidate smoke test`
105. `[Story] US-E20-01 - Review the project as a portfolio artifact`
106. `[Technical Story] TS-E20-01 - Create traceable documentation set`
107. `[Task] T-E20-01 - Write portfolio README`
108. `[Task] T-E20-02 - Create architecture diagram and ADRs`
109. `[Task] T-E20-03 - Create testing and performance reports`
110. `[Task] T-E20-04 - Create portfolio demo script`
111. `[Story] US-E21-01 - Plan future product growth`
112. `[Technical Story] TS-E21-01 - Define future extension contracts without implementing them`
113. `[Task] T-E21-01 - Document cloud sync extension path`
114. `[Task] T-E21-02 - Document OCR and receipt import path`
115. `[Task] T-E21-03 - Document export and accountant/freelancer path`
116. `[Task] T-E21-04 - Document monetization/entitlement path`

## Newly Created Issues

None in this update.

## Assumptions and Limitations

- Epic rows were not created as issues because the requested importer scope is User Stories, Technical Stories, Tasks, and Subtasks.
- Existing issues use an older numbering scheme for some areas. Matching therefore uses both exact normalized title and backlog ID to avoid duplicates.
- Suggested labels are written into issue bodies instead of being applied through the API, so the script will not fail if labels do not already exist in GitHub.
- The script assigns milestones by the four allowed existing milestone names only.
- Parent checklist updates occur only when the script actually creates or finds the child Task issues and can resolve the parent issue by backlog ID.
