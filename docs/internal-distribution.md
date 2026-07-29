# Internal distribution

Folentra uses `.github/workflows/internal-distribution.yml` to build and send a
signed, minified `release` APK to Firebase App Distribution. The workflow is
separate from PR validation and Play Store publication, runs only by manual
dispatch or a `beta-*` tag, and is protected by the `firebase-internal`
environment.

## Release gate

Before upload, the workflow:

1. verifies every required secret without printing its value;
2. reconstructs `google-services.json`, the service account, and release
   keystore in runner-only storage;
3. validates that the Firebase config contains both
   `com.mojtaba.folentra` and `com.mojtaba.folentra.debug`, and that
   `FIREBASE_APP_ID` belongs to the release package;
4. runs the Firebase validator tests, release lint, JVM/shared tests, release
   signing validation, and release assembly;
5. verifies the APK signature with Android `apksigner`;
6. uploads the APK and R8 mapping as private GitHub Actions artifacts; and
7. distributes the APK through the Firebase CLI, then removes all temporary
   credentials in an `always()` cleanup step.

No debug APK is distributed. Normal debug builds keep Crashlytics collection
disabled; the release build enables it only when a valid runtime Firebase config
was injected.

## Protected secrets

Configure these in the `firebase-internal` GitHub environment:

```text
FIREBASE_APP_ID
FIREBASE_GOOGLE_SERVICES_JSON
FIREBASE_SERVICE_ACCOUNT_JSON
FIREBASE_TESTER_GROUPS
FOLENTRA_RELEASE_STORE_BASE64
FOLENTRA_RELEASE_STORE_PASSWORD
FOLENTRA_RELEASE_KEY_ALIAS
FOLENTRA_RELEASE_KEY_PASSWORD
```

`FIREBASE_APP_ID` must identify the Firebase Android client for
`com.mojtaba.folentra`. `FIREBASE_TESTER_GROUPS` contains group aliases, not
tester email addresses. The repository must never contain
`google-services.json`, a service-account file, or signing material.

Use environment reviewers if distribution needs an approval gate. Limit the
service account to App Distribution permissions and rotate any credential that
appears in logs or artifacts.

## Artifacts and notes

Successful runs retain:

```text
folentra-internal-release-apk-<version>-<code>-<ref>-<run>
folentra-internal-release-mapping-<version>-<code>-<ref>-<run>
```

Manual release notes or the tag commit message are passed to Firebase through a
temporary file. They must not contain tester identities, financial data,
secrets, stack traces, or raw diagnostics. Workflow summaries intentionally omit
notes, tester groups, and credential-derived values.

## Scope

This pipeline supplies a repeatable internal release candidate and supports
issues #15, #16, #17, and #134. It does not publish to Play Store, promote a
staged rollout, create Firebase projects, provision repository secrets, install
the tester build, or prove a console-side Crashlytics event. Those operations
require project-owner access and runtime evidence.
