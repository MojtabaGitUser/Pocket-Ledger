# Play Store Readiness And App Content Checklist

This checklist records the current Play Console app content position for
Folentra based on the repository state. It supports `T-E19-04` under `E-19 - Play
Store Readiness` and must be re-reviewed before every public Play Store
submission.

Do not copy these answers into Play Console without checking the current merged
release manifest, Firebase configuration, privacy policy, and release
checklist. If app behavior changes, update this document and
`docs/privacy-policy.md` before submission.

## Evidence Reviewed

- App source manifest:
  `androidApp/app/src/main/AndroidManifest.xml`.
- Generated release merged manifest:
  `androidApp/app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml`.
- Debug merged manifest for debug/test-only permission comparison:
  `androidApp/app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`.
- Backup and device-transfer XML:
  `androidApp/app/src/main/res/xml/backup_rules.xml` and
  `androidApp/app/src/main/res/xml/data_extraction_rules.xml`.
- Firebase setup:
  `androidApp/app/google-services.json`,
  `androidApp/app/build.gradle.kts`, and
  `androidApp/gradle/libs.versions.toml`.
- Runtime analytics boundary:
  `androidApp/app/src/main/java/com/mojtaba/folentra/AppGraph.kt` and
  `androidApp/core/analytics`.
- Privacy, release, CI/CD, security, logging, accessibility, signing, install,
  and internal distribution docs under `docs/` and `androidApp/docs/`.

No `Pocket_Ledger_Complete_Backlog.docx` file is present in the repository.
Backlog context that is present lives in `docs/github-issue-import-report.md`.

## Play Console App Content Checklist

| Area | Current declaration basis | Status |
| --- | --- | --- |
| Privacy policy | Draft policy exists at `docs/privacy-policy.md`. README links to it. No hosted public URL is committed. | Required pre-release action: publish the policy at a public HTTPS URL and paste that URL into Play Console. |
| Public support contact | `support.folentra@gmail.com` is documented in both privacy-policy formats. | Use the same monitored address in the Play Store listing and keep account recovery and two-factor authentication enabled. |
| Data Safety | Local ledger data is stored on device in Room. Firebase dependencies are present, but Google Services and Crashlytics are disabled until Folentra Firebase clients are installed. Product event logging remains no-op in release. | Declare conservatively and re-review after installing the replacement Firebase config. |
| App access | The current app has no account login, paid wall, institution login, server-backed profile, or restricted content gate. Optional app lock uses Android system authentication after installation when enabled by the user. | Mark no special app access instructions unless future features add gated access. |
| Ads | No ad UI, ad network integration, or ad-serving feature is implemented. Firebase/Google SDKs contribute advertising/ad-services identifier permissions for analytics/attribution capability, not app-served ads. | Declare no ads if Play Console asks whether the app contains ads. |
| Content ratings | App is a personal finance ledger and budget utility. It does not include user-generated public content, gambling, social networking, shopping, or regulated investment/banking flows. | Complete the questionnaire as a finance/productivity utility based on actual screenshots and features. |
| Target audience and children | The privacy policy states the app is not designed for children. The product is a personal finance utility for users managing their own ledger. | Target adults/general finance users; do not mark as child-directed. |
| Financial features | The app records local transactions, budgets, categories, tags, summaries, search, private monthly insights, and user-confirmed smart autofill. It does not provide banking, lending, investing, money transmission, payments, credit, tax filing, financial advice, or regulated financial services. | Declare as personal finance tracking only. Do not overstate regulated services or AI advice. |
| Permissions declaration | Release merged manifest permissions are listed below. No contacts, camera, location, photos, calendar, SMS, phone, microphone, or notification permission is in the release merged manifest. | Use release manifest only for production declarations. |
| Data collection | Local financial data stays in app-private storage unless Android backup/device transfer or Firebase SDK behavior applies. No user account data, contact data, payment-card credentials, cloud sync data, import/export payload, or remote AI data path is implemented. | Declare local financial data handling and Firebase SDK metadata conservatively. |
| Data sharing | Current app code does not send ledger records to a Folentra server. Firebase/Google SDKs may receive technical analytics or attribution data. Firebase App Distribution shares debug APKs with authorized testers through CI, not runtime app code. | Do not claim ledger records are shared. Do disclose Firebase/Google SDK behavior where Play Console requires it. |
| Encryption and security | Room database is app-private but not encrypted by Folentra. Sensitive preferences use AndroidX Security Crypto. Optional app lock uses Android system authentication. No network transport claim should be made for ledger sync because sync is not implemented. | Do not claim full database encryption or cloud transport protection for ledger data. |
| App category | Personal finance, budgeting, or finance utility. | Final category should match store listing copy and screenshots. |
| Internal testing readiness | Release candidate workflow creates release APK/AAB artifacts for Play Console internal testing. Firebase App Distribution distributes debug APKs only and remains separate from Play Store artifacts. | Use release AAB for Play Console internal testing. |
| Backup/device-transfer behavior | `allowBackup=true`; `dataExtractionRules` and `fullBackupContent` are configured with explicit deny-by-default rules from #227. Ledger database files, encrypted preferences, app-private files, caches, logs, temp/debug/generated files, and external app files are excluded from cloud backup and device transfer. | Review before release; claim only local-first backup-ready profile foundation unless encrypted backup and restore are separately implemented. |
| Release diagnostics/privacy safety | Debug Health exists only in debug navigation with a release source-set stub. CI/CD and Debug Health docs prohibit secrets, tester emails, stack traces, IDs, and sensitive ledger values in diagnostics. | Keep release diagnostics hidden and recheck before submission. |

## Release Permissions

The current release merged manifest declares these permissions:

- `android.permission.USE_BIOMETRIC` and
  `android.permission.USE_FINGERPRINT`: optional app lock through Android
  system authentication.
- `android.permission.INTERNET` and
  `android.permission.ACCESS_NETWORK_STATE`: Firebase Analytics and
  network-aware SDK behavior. Current app code does not use these permissions
  for ledger sync.
- `android.permission.WAKE_LOCK`,
  `android.permission.RECEIVE_BOOT_COMPLETED`, and
  `android.permission.FOREGROUND_SERVICE`: AndroidX WorkManager
  infrastructure. Current production background jobs are disabled by default
  through feature flags.
- `com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE`:
  Google/Firebase install referrer or attribution capability.
- `com.google.android.gms.permission.AD_ID`,
  `android.permission.ACCESS_ADSERVICES_ATTRIBUTION`, and
  `android.permission.ACCESS_ADSERVICES_AD_ID`: Google/Firebase analytics or
  attribution capability. The app does not serve ads or store ledger records
  using these identifiers.
- `com.mojtaba.folentra.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`: app
  scoped AndroidX runtime permission for non-exported dynamic receivers.

The debug merged manifest additionally includes
`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, and
`POST_NOTIFICATIONS` from debug tooling. Those are debug/test-only and must not
be treated as release permissions.

## Firebase Analytics And Data Safety Notes

Firebase Analytics is included through the Firebase BoM and
`libs.firebase.analytics`. The checked-in `google-services.json` still contains
clients for the retired package IDs. `app/build.gradle.kts` disables Google
Services and Crashlytics unless both `com.mojtaba.folentra` and
`com.mojtaba.folentra.debug` clients are present. Dependencies may still
contribute Firebase/Google manifest components and permissions, so Data Safety
must remain conservative.

Folentra product event logging is not wired to Firebase Analytics in the
current app code. Release builds construct `NoOpProductAnalyticsLogger`; debug
builds construct a safe debug logger that writes mapped typed events only if
future code logs those events. A repository search found product event logging
only in analytics unit tests and logger construction, not in feature runtime
flows.

For Play Console Data Safety, treat Firebase SDK behavior as potentially
collecting technical analytics or attribution data even though Folentra
does not send product event payloads or ledger records to Firebase from app
code.

## Product Event Taxonomy Privacy Safety

The taxonomy in `docs/product-event-taxonomy.md` and
`androidApp/core/analytics` allows only typed event names and constrained
parameters such as screen name, source, result, error type, count bucket,
amount bucket, currency-present flag, recurring flag, build type, app version,
and feature flag state.

E-12 AI features are local-only. Current provider shells for on-device models are unavailable by default, and deterministic rule-based fallback handles monthly insights, semantic search ranking, and smart autofill without remote AI calls or AI secrets.

Product analytics events must not contain:

- Transaction descriptions.
- Account names.
- Category names.
- Merchant names.
- Notes.
- Exact amounts.
- Exact balances.
- User-entered text.
- Raw IDs.
- Secrets.
- Stack traces.
- Service account data.

Do not add provider wiring, new event names, or new parameters without updating
the taxonomy tests, this checklist, and the privacy policy.

## Backup And Device Transfer

Current configuration:

- `android:allowBackup="true"`.
- `android:dataExtractionRules="@xml/data_extraction_rules"`.
- `android:fullBackupContent="@xml/backup_rules"`.
- `backup_rules.xml` has active explicit deny-by-default rules for pre-Android
  12 backup behavior.
- `data_extraction_rules.xml` has active explicit deny-by-default rules for
  Android 12+ `cloud-backup` and `device-transfer` behavior.

No app-private Folentra files are intentionally included in automatic
Android cloud backup or device-to-device transfer. Ledger database files,
SQLite sidecars, encrypted sensitive preferences, local-only settings, caches,
logs, temp files, debug artifacts, generated reports, device-protected storage,
and external app files are excluded. This is the safe default for #227 because
#81 implements only the local-first backup-ready profile foundation, not
encrypted ledger backup or restore. Re-review this section before public
release and after any future backup/profile work.

## Required Pre-Release Actions

- Replace the release blockers in `docs/privacy-policy.html`, publish that
  standalone page at a public HTTPS URL, and enter the URL in Play Console.
- Follow `docs/release/privacy-publication-handoff.md` so the hosted policy,
  canonical Markdown policy, Store Listing email, and Play Console URL match.
- Replace the privacy policy support-contact placeholder with a real public
  support contact and use the same contact in the Play Store listing.
- Re-review Android backup and device-transfer rules for ledger database files,
  app settings, and encrypted preferences before each public release.
- Rebuild or regenerate the release merged manifest after any dependency,
  manifest, Firebase, WorkManager, or build-type change and update the
  permission list if it changes.
- Review Firebase Analytics project settings and Play Console Data Safety
  answers immediately before submission.
- Complete Play Console content rating, target audience, financial features,
  data safety, app access, ads, and permissions forms using this checklist and
  the final release artifact.

## Store Listing And Assets

Repository-ready listing copy, release notes, screenshot sources, asset file
names, expected dimensions to verify, acceptance matrix, and manual Play
Console steps are tracked in `docs/release/play-store-assets.md`.

Current #131 status:

- Store listing copy is prepared in the repository.
- App name and launcher icon resources compile from the Android app resources.
- A screenshot/feature-graphic capture plan exists for deterministic sample-safe
  data.
- Final phone/tablet screenshots, opaque high-res icon, and feature graphic are
  committed under `docs/release/assets/play-store/` and validated in release CI.
- Play Console upload and final policy review remain release-owner work.

Do not claim final Play Store graphic upload or Play Console approval from this
repository state alone.


## Release-Ready Install Evidence

#128 is tracked by `docs/release/release-ready-install.md` and
`docs/release/smoke-test.md`.

Current #128 status:

- Repository runbook defines signed release APK/AAB, Play internal testing AAB,
  benchmark release-like APK, and debug APK boundaries.
- Release signing validation is documented and must pass for true signed release
  artifacts.
- Benchmark APK remains the closest local release-like installer when signing
  secrets are unavailable.
- Actual install, launch, logcat, and core-flow smoke evidence still requires a
  named physical device or emulator.

Do not claim #128 complete until install evidence is recorded in the smoke-test
record.
## Optional Backup-Ready Profile

The #81 backup-ready profile foundation is documented in
`docs/backup-ready-profile.md`. The current app exposes only a local-first
opt-in state and prerequisite model. It does not implement production account
login, passkey recovery, cloud sync, encrypted ledger backup payloads, or a
restore contract. The #227 deny-by-default Android backup policy remains the
correct Play Store disclosure basis.

## Issue Traceability

- #129: release hardening checklist is expanded in
  `docs/release/release-checklist.md`; signed release, Play Console upload,
  public hosting, legal review, and hardware performance checks remain manual
  release gates.
- #131: store listing copy, validation, icon, feature graphic, and phone/tablet
  screenshots are repository-ready; Play Console upload remains manual.
- #132: privacy policy is maintained in `docs/privacy-policy.md` and must be
  hosted before public release.
- #7: local data security criteria are mapped in
  `androidApp/docs/security-model.md`; do not claim full database encryption or
  Play Integrity enforcement.
- #81: optional backup-ready profile foundation is implemented and documented.
  Ledger data remains excluded from Android backup and device transfer.
- #128: release-ready install runbook is repository-ready; actual close
  evidence requires a named device/emulator or Play internal testing install.
- #17: parent Play Store release story remains open until release signing,
  install evidence, hosted privacy URL, app-content declarations, final assets,
  and release checklist review are complete.
