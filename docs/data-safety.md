# Google Play Data Safety working declaration

This document is a source-of-truth worksheet, not a submitted Play Console
form. Recheck the final merged release manifest, Firebase project settings,
SDK versions, and Play Console wording immediately before submission.

## Release behavior

Folentra stores ledger transactions, amounts, merchants, notes, categories,
tags, budgets, and search text locally. App code does not intentionally send
those values to Firebase Analytics, Crashlytics, App Distribution, or a
Folentra server.

Firebase Crashlytics collection is enabled only in a correctly configured
release build. Debug and benchmark variants disable automatic collection.
Release reports use sanitized, bounded, allowlisted metadata and may include
technical stack frames.

Firebase App Distribution operates in GitHub Actions and distributes signed
release APKs to authorized testers. Tester identities, groups, service-account
credentials, signing material, and Firebase configuration are CI secrets and
are not embedded in app diagnostics or uploaded as public artifacts.

## Conservative Play Console answers

Validate these against Firebase's current SDK disclosure before submitting:

| Data type | Collected in configured release | Shared | Purpose |
| --- | --- | --- | --- |
| Crash logs | Yes | Treat Firebase as a service provider; confirm Console interpretation | App health, diagnostics, fraud/security investigation |
| Diagnostics / other app performance data | Yes or potentially collected by Firebase SDK behavior | Confirm service-provider treatment | App health and performance |
| Device or other IDs | Potentially, including Firebase installation/app-instance identifiers | Confirm service-provider treatment | Analytics, app health, service operation |
| App interactions | Potentially through Firebase Analytics automatic events | Confirm Firebase project settings | Analytics |
| Financial information | No intentional off-device collection | No | Local app functionality only |
| In-app search history | No intentional off-device collection | No | Local search only |
| Other user-generated content such as notes | No intentional off-device collection | No | Local app functionality only |

Do not select “no data collected” for a public release merely because Folentra
does not log custom product events. Third-party SDK collection must be included.
Do not claim that ledger data is encrypted at rest: Room uses app-private
storage but the ledger database is not currently encrypted by Folentra.

## Release verification

- [ ] The final `google-services.json` contains exactly reviewed Folentra
  release/debug clients and is not tracked by Git.
- [ ] Crashlytics automatic collection is false in debug and benchmark and
  true only in configured release.
- [ ] A synthetic non-fatal event contains no financial, account, search,
  credential, tester, Firebase-secret, or signing values.
- [ ] Firebase Analytics automatic collection/settings are reviewed in the
  Firebase console.
- [ ] The final merged release manifest permissions match Play Console.
- [ ] The privacy policy, Data Safety form, and actual release behavior agree.
