# Pocket Ledger Privacy Policy

Effective date: July 5, 2026

Contact: [add public support email before Play Store submission]

Pocket Ledger is a personal finance app for recording transactions, budgets,
categories, tags, and related ledger summaries. This policy describes the
behavior implemented in the current Android app package
`com.mojtaba.pocketledger`.

This policy must be reviewed and updated before public Play Store publication if
app behavior, third-party services, support contact details, hosted policy URL,
or data disclosures change. Before submission, Pocket Ledger must publish this
policy at a public HTTPS URL, add that URL to Play Console, and replace the
support-contact placeholder with the same public contact used in the Play Store
listing.

## Information Stored Locally

Pocket Ledger stores ledger data in the app-private Room database
`pocket-ledger.db` on the user's device. This local data can include:

- Transactions, including amount, currency, date, type, merchant, note, source,
  recurring state, and timestamps.
- Budgets, including budget name, amount, currency, period, active state, and
  related category.
- Categories, tags, and transaction-tag links.
- Local app settings and feature-flag state used by the app.

Pocket Ledger also stores app-lock preference state in encrypted Android shared
preferences. The current app-lock preference records whether app lock is enabled.
Future passkey/session preference keys are defined in code, but the current app
does not implement user accounts, passkeys, or server-backed authentication.

## Financial Data Handling

Pocket Ledger is designed to keep financial records local unless a feature is
explicitly changed to require external processing. In the current app:

- There is no account login.
- There is no server-backed user profile.
- There is no cloud sync implementation.
- There is no banking integration.
- There is no import or export flow implemented.
- There is no remote AI provider or network AI request path.
- Private monthly insights, semantic search ranking, and smart autofill use on-device provider contracts with deterministic local fallback. Current on-device model provider shells are unavailable by default; the implemented fallback runs locally and offline.

The local Room database is protected by the Android app sandbox, but it is not
currently encrypted by Pocket Ledger. App-lock helps prevent casual access to
screens when enabled, but it does not encrypt ledger records.

Android backup and device-transfer rules are explicit and privacy-safe by
default. Pocket Ledger excludes app-private ledger data, the Room database,
SQLite sidecar files, encrypted sensitive preferences, local-only settings,
caches, logs, temporary files, debug files, generated reports, and external app
files from automatic Android cloud backup and Android device-to-device transfer.
No optional backup-ready profile is implemented in the current app.

## Information Not Collected By Current App Features

The current app does not ask users to provide an email address, phone number,
mailing address, government identifier, contact list, photos, camera input,
location, or payment-card credentials.

Pocket Ledger does not intentionally collect transaction descriptions, merchant
names, account names, category names, exact balances, exact amounts, notes,
search text, or budget values for analytics, crash reports, logs, or release
diagnostics.

## Analytics And Product Events

Pocket Ledger includes Firebase Analytics as an Android dependency and has
Firebase app configuration for the release and debug application IDs. The
project also defines a typed product event taxonomy in `:core:analytics` for
future analytics, observability, app-health reporting, and release monitoring.

Current behavior is conservative:

- Release and benchmark builds use no-op product analytics behavior in app code.
- Debug builds may map typed product events to sanitized event names and approved
  parameters through the existing safe logger if future code logs those events.
- Product event logging is not currently wired from Pocket Ledger feature code to
  Firebase Analytics.

If product analytics are enabled in a future release, approved event names and
parameters are designed to avoid sensitive financial data. Approved parameters
use generic values such as screen name, source, result, error type, count bucket,
amount bucket, currency-present flag, recurring flag, build type, app version,
and feature-flag state. They must not include transaction descriptions,
merchant names, account names, user-created category names, tags, notes, exact
amounts, exact balances, emails, raw database IDs, device identifiers, Firebase
IDs, tokens, stack traces, or service-account data.

Because Firebase Analytics is present, Google/Firebase SDK behavior may involve
app instance identifiers, app version, device/app metadata, install referrer or
attribution data, network state, and advertising or ad-services identifiers as
permitted by the Android platform and Firebase SDK configuration. Pocket Ledger
should review Firebase Analytics settings and Play Console data disclosures
before production publication.

## Crash Reporting And Diagnostics

Pocket Ledger does not currently include the Firebase Crashlytics runtime
dependency and does not intentionally upload crash reports from app code.

Logging is centralized behind a safe logging abstraction. Debug builds allow
sanitized debug, info, warning, and error logs. Release builds allow sanitized
warning and error logs only. Logging policy forbids transaction amounts,
merchant names, notes, tags, search text, budget values, credentials, tokens,
secrets, encryption keys, encrypted payloads, raw AI prompts, provider diagnostics, and generated
sensitive content.

If crash reporting is added later, this policy must be updated to describe the
provider and the diagnostic data collected, such as app version, device type,
operating system version, non-sensitive technical logs, and crash stack traces.
Sensitive financial content must not be intentionally included in crash reports.

## Firebase App Distribution And Internal Testing

Pocket Ledger has a GitHub Actions workflow for Firebase App Distribution. This
workflow can distribute debug APKs to authorized internal testers after CI
validation and only when required Firebase secrets are configured in GitHub
Actions.

App Distribution is a CI/CD process, not runtime app code. The app does not
upload builds, tester lists, service-account credentials, tokens, or Firebase
App Distribution data from the installed app. Tester access and distribution are
managed through Firebase and GitHub Actions for authorized internal testing.

## Permissions Used By The App

The production app source manifest declares the launcher activity and app backup
configuration. Additional permissions appear in the merged release manifest from
AndroidX, Firebase, WorkManager, and Biometric dependencies:

- `android.permission.USE_BIOMETRIC` and `android.permission.USE_FINGERPRINT`:
  support optional app lock through Android system authentication.
- `android.permission.INTERNET` and `android.permission.ACCESS_NETWORK_STATE`:
  support Firebase Analytics and network-aware SDK behavior. The current app
  does not use these permissions to sync ledger data.
- `android.permission.WAKE_LOCK`, `android.permission.RECEIVE_BOOT_COMPLETED`,
  and `android.permission.FOREGROUND_SERVICE`: contributed by WorkManager for
  background-work infrastructure. Production background jobs are disabled by
  default in current feature flags.
- `com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE`:
  supports install-referrer or attribution behavior used by Google/Firebase SDKs.
- `com.google.android.gms.permission.AD_ID`,
  `android.permission.ACCESS_ADSERVICES_ATTRIBUTION`, and
  `android.permission.ACCESS_ADSERVICES_AD_ID`: contributed by Google/Firebase
  SDKs for analytics/attribution capabilities. Pocket Ledger does not use these
  identifiers to store financial records.
- `<applicationId>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`: an app-scoped
  permission used by AndroidX runtime components for non-exported dynamic
  receivers.

The release app does not request contacts, camera, location, photos, calendar,
SMS, phone, microphone, or notification permissions in the current merged
release manifest.

Debug and test-only builds may include additional permissions from development
or test tooling, such as storage or notification permissions for LeakCanary or
macrobenchmark infrastructure. Those permissions are not part of the release app
privacy claim.

## Third-Party Services And SDKs

The current Android app includes or uses these relevant third-party components:

- Firebase Analytics and Google Services configuration.
- Firebase App Distribution through GitHub Actions for internal debug APK
  distribution.
- AndroidX Room for local database storage.
- AndroidX Security Crypto for encrypted sensitive preferences.
- AndroidX Biometric for optional app lock through Android system
  authentication.
- AndroidX WorkManager for background-work infrastructure.
- LeakCanary in debug builds only.
- Paparazzi, Macrobenchmark, JUnit, and AndroidX test libraries for development
  and CI validation.

Pocket Ledger does not currently include Firebase Crashlytics in the app module.

## Data Sharing

Pocket Ledger does not sell personal data. Current app code does not send ledger
records to Pocket Ledger servers because no Pocket Ledger server, account, or
cloud sync feature is implemented.

Firebase/Google SDKs may receive technical analytics or attribution data when
their SDK behavior is active. Internal testing builds may be distributed through
Firebase App Distribution to authorized testers. CI/CD workflows must not expose
service-account files, API tokens, tester emails, keystores, passwords, or
private release data in logs or artifacts.

## Data Retention

Ledger records remain on the user's device until the user deletes them in the
app, clears app storage, or uninstalls the app. Pocket Ledger's Android backup
and data-extraction rules exclude the local ledger database from automatic
cloud backup and device-to-device transfer by default because a backup-ready
profile is not implemented.

Firebase or Google SDK data retention, if collected by enabled SDK behavior, is
governed by the relevant Firebase/Google service settings and policies.

## Data Deletion And User Control

Users can delete ledger records through implemented app screens where deletion
is available. Users can also clear app storage or uninstall the app through
Android system settings to remove local app data from the device.

There is currently no Pocket Ledger account portal or server-side deletion
request process because the app does not implement user accounts or server-side
ledger storage.


## Android Backup And Device Transfer

The Android manifest enables backup plumbing with explicit rule files:
`backup_rules.xml` for pre-Android 12 backup behavior and
`data_extraction_rules.xml` for Android 12+ cloud backup and device-to-device
transfer.

Pocket Ledger does not intentionally include app-private data in automatic
Android cloud backup or device-to-device transfer. The rules exclude the local
Room ledger database `pocket-ledger.db`, SQLite sidecar files, encrypted
sensitive preferences, app-private files, shared preferences, external app
files, device-protected storage, caches, logs, temporary files, debug artifacts,
and generated reports.

This is a conservative default for #227 because the app does not currently
implement the optional backup-ready profile tracked by #81. It supports the
local-data security goals related to #7, but it does not complete #7 or #81 by
itself.

## Security

Pocket Ledger uses Android app-private storage for the local database and
AndroidX Security Crypto for sensitive preference storage. Optional app lock uses
Android system authentication when enabled and available. Release builds use
release-safe logging, and debug-only diagnostics are excluded from release
navigation.

No app can guarantee complete security. Pocket Ledger does not currently encrypt
the Room database, prevent screenshots, prevent screen recording, protect
against compromised devices, or replace the security of the user's device lock
screen.

## Children's Privacy

Pocket Ledger is a personal finance utility and is not designed for children.
The app does not knowingly request personal information from children.

## Changes To This Policy

This policy should be updated when Pocket Ledger adds or changes analytics,
crash reporting, cloud sync, account login, import/export, AI providers,
permissions, backup behavior, third-party services, or Play Store disclosures.
The effective date at the top of this document should be updated when the policy
changes.

