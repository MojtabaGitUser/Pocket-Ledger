# Folentra Brand And Package Migration

The product identity is now **Folentra**.

## Stable identifiers

- Android release application ID: `com.mojtaba.folentra`
- Android debug application ID: `com.mojtaba.folentra.debug`
- Root Kotlin/Java package: `com.mojtaba.folentra`
- Gradle root project: `Folentra`
- Deep-link scheme: `folentra://`
- Release environment/Gradle property prefix: `FOLENTRA_`

Changing the Android application ID creates a different installed application.
Data belonging to `com.mojtaba.pocketledger` is not automatically migrated to
Folentra. This is intentional before the first public release; do not publish
under either ID until the final identity is approved.

## Firebase handoff

The checked-in `app/google-services.json` is retained only as evidence of the
retired Firebase clients. The build verifies that both Folentra package IDs are
present before applying Google Services or Crashlytics. Until then, Firebase
runtime initialization and crash collection are fail-closed and disabled.

In Firebase Console:

1. Register Android apps for `com.mojtaba.folentra` and
   `com.mojtaba.folentra.debug`.
2. Download one replacement `google-services.json` containing both clients.
3. Replace `androidApp/app/google-services.json` without editing app IDs by hand.
4. Re-run debug/release builds and verify Google Services processing,
   Crashlytics collection policy, Analytics/Data Safety disclosures, and App
   Distribution's `FIREBASE_APP_ID` secret.

## CI and release secrets

Repository property and signing names now use `FOLENTRA_VERSION_*`,
`FOLENTRA_RELEASE_*`, and `FOLENTRA_REQUIRE_RELEASE_SIGNING`. Rename the matching
GitHub Actions secrets before running a release candidate. Existing keystore
material can be reused only if it is intentionally approved for the new app
identity.