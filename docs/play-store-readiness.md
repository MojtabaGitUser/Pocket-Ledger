# Play Store Readiness And App Content Checklist

This checklist records the current Play Console app content position for Pocket
Ledger based on the repository state. It supports `T-E19-04` under `E-19 - Play
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
  `androidApp/app/src/main/java/com/mojtaba/pocketledger/AppGraph.kt` and
  `androidApp/core/analytics`.
- Privacy, release, CI/CD, security, logging, accessibility, signing, and
  internal distribution docs under `docs/` and `androidApp/docs/`.

No `Pocket_Ledger_Complete_Backlog.docx` file is present in the repository.
Backlog context that is present lives in `docs/github-issue-import-report.md`.

## Play Console App Content Checklist

| Area | Current declaration basis | Status |
| --- | --- | --- |
| Privacy policy | Draft policy exists at `docs/privacy-policy.md`. README links to it. No hosted public URL is committed. | Required pre-release action: publish the policy at a public HTTPS URL and paste that URL into Play Console. |
| Public support contact | Privacy policy currently uses `[add public support email before Play Store submission]`. No public support email is documented elsewhere. | Required pre-release action: add a real public support contact to the privacy policy and Play Store listing. |
| Data Safety | Local ledger data is stored on device in Room. Firebase Analytics dependency and Google Services config are present, and Firebase measurement components appear in the release manifest. Product event logging is no-op in release and not wired to Firebase, but Firebase SDK behavior may collect app/device, app instance, install/referrer, attribution, and advertising/ad-services data depending on Firebase settings. | Declare conservatively. Do not claim that no data is collected while Firebase Analytics is present. |
| App access | The current app has no account login, paid wall, institution login, server-backed profile, or restricted content gate. Optional app lock uses Android system authentication after installation when enabled by the user. | Mark no special app access instructions unless future features add gated access. |
| Ads | No ad UI, ad network integration, or ad-serving feature is implemented. Firebase/Google SDKs contribute advertising/ad-services identifier permissions for analytics/attribution capability, not app-served ads. | Declare no ads if Play Console asks whether the app contains ads. |
| Content ratings | App is a personal finance ledger and budget utility. It does not include user-generated public content, gambling, social networking, shopping, or regulated investment/banking flows. | Complete the questionnaire as a finance/productivity utility based on actual screenshots and features. |
| Target audience and children | The privacy policy states the app is not designed for children. The product is a personal finance utility for users managing their own ledger. | Target adults/general finance users; do not mark as child-directed. |
| Financial features | The app records local transactions, budgets, categories, tags, summaries, search, private monthly insights, and user-confirmed smart autofill. It does not provide banking, lending, investing, money transmission, payments, credit, tax filing, financial advice, or regulated financial services. | Declare as personal finance tracking only. Do not overstate regulated services or AI advice. |
| Permissions declaration | Release merged manifest permissions are listed below. No contacts, camera, location, photos, calendar, SMS, phone, microphone, or notification permission is in the release merged manifest. | Use release manifest only for production declarations. |
| Data collection | Local financial data stays in app-private storage unless Android backup/device transfer or Firebase SDK behavior applies. No user account data, contact data, payment-card credentials, cloud sync data, import/export payload, or remote AI data path is implemented. | Declare local financial data handling and Firebase SDK metadata conservatively. |
| Data sharing | Current app code does not send ledger records to a Pocket Ledger server. Firebase/Google SDKs may receive technical analytics or attribution data. Firebase App Distribution shares debug APKs with authorized testers through CI, not runtime app code. | Do not claim ledger records are shared. Do disclose Firebase/Google SDK behavior where Play Console requires it. |
| Encryption and security | Room database is app-private but not encrypted by Pocket Ledger. Sensitive preferences use AndroidX Security Crypto. Optional app lock uses Android system authentication. No network transport claim should be made for ledger sync because sync is not implemented. | Do not claim full database encryption or cloud transport protection for ledger data. |
| App category | Personal finance, budgeting, or finance utility. | Final category should match store listing copy and screenshots. |
| Internal testing readiness | Release candidate workflow creates release APK/AAB artifacts for Play Console internal testing. Firebase App Distribution distributes debug APKs only and remains separate from Play Store artifacts. | Use release AAB for Play Console internal testing. |
| Backup/device-transfer behavior | `allowBackup=true`; `dataExtractionRules` and `fullBackupContent` are configured, but both XML files are template-style and do not define explicit ledger or preference include/exclude rules. | Required pre-release action: finalize backup/device-transfer policy before public release. |
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
- `com.mojtaba.pocketledger.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`: app
  scoped AndroidX runtime permission for non-exported dynamic receivers.

The debug merged manifest additionally includes
`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, and
`POST_NOTIFICATIONS` from debug tooling. Those are debug/test-only and must not
be treated as release permissions.

## Firebase Analytics And Data Safety Notes

Firebase Analytics is included through the Firebase BoM and
`libs.firebase.analytics`, and `google-services.json` contains Firebase clients
for `com.mojtaba.pocketledger` and `com.mojtaba.pocketledger.debug`. The release
merged manifest includes Firebase measurement services, receiver, provider,
install referrer permission, network permissions, and advertising/ad-services
permissions.

Pocket Ledger product event logging is not wired to Firebase Analytics in the
current app code. Release builds construct `NoOpProductAnalyticsLogger`; debug
builds construct a safe debug logger that writes mapped typed events only if
future code logs those events. A repository search found product event logging
only in analytics unit tests and logger construction, not in feature runtime
flows.

For Play Console Data Safety, treat Firebase SDK behavior as potentially
collecting technical analytics or attribution data even though Pocket Ledger
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
- `backup_rules.xml` is a sample/template file with no active include or
  exclude rules.
- `data_extraction_rules.xml` has a `cloud-backup` block with only TODO
  comments and no active device-transfer block.

Because backup is enabled and explicit include/exclude rules are not finalized,
do not claim backup is disabled or that ledger data is excluded from cloud
backup or device transfer. Before public release, choose and implement the
intended policy, then update this document, `docs/privacy-policy.md`, and
`androidApp/docs/security-model.md`.

## Required Pre-Release Actions

- Publish `docs/privacy-policy.md` at a public HTTPS URL and enter that URL in
  Play Console.
- Replace the privacy policy support-contact placeholder with a real public
  support contact and use the same contact in the Play Store listing.
- Finalize Android backup and device-transfer rules for ledger database files,
  app settings, and encrypted preferences.
- Rebuild or regenerate the release merged manifest after any dependency,
  manifest, Firebase, WorkManager, or build-type change and update the
  permission list if it changes.
- Review Firebase Analytics project settings and Play Console Data Safety
  answers immediately before submission.
- Complete Play Console content rating, target audience, financial features,
  data safety, app access, ads, and permissions forms using this checklist and
  the final release artifact.
