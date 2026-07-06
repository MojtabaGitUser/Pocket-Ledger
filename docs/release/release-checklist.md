# Release Readiness Checklist

Use this checklist before treating a Pocket Ledger artifact as release-ready.
PR validation and release-candidate workflows support this process, but they do
not replace human review for signing, privacy, and Play Store readiness.

## Signing And Versioning

- [ ] Build variants were reviewed: `debug` is for local/internal diagnostics,
  `benchmark` is non-debuggable/profileable and debug-signed for measurement,
  and `release` is the Play Store candidate path.
- [ ] `POCKET_LEDGER_VERSION_CODE` was incremented for this Play Store upload
  candidate.
- [ ] `POCKET_LEDGER_VERSION_NAME` was reviewed and matches the release train.
- [ ] Release signing is configured for release-ready APK/AAB builds.
- [ ] The upload keystore is stored securely outside the repository.
- [ ] Required GitHub Actions signing secrets are configured in a protected
  repository or environment scope.
- [ ] No `.jks`, `.keystore`, key alias, password, service account JSON, Play
  Store credential, Firebase credential, private tester list, or private release
  data is committed or uploaded as an artifact.
- [ ] `:app:validateReleaseSigning` passed for a signed release-ready build.

## Candidate Build

- [ ] Final validation used the Android app module at `androidApp/app` and the
  production application ID `com.mojtaba.pocketledger`.
- [ ] Release candidate workflow completed successfully.
- [ ] Release APK and AAB artifacts were generated.
- [ ] R8 mapping artifacts were retained with the candidate record.
- [ ] `lintRelease`, JVM/shared tests, release assembly, bundle generation, and
  benchmark artifact assembly passed.
- [ ] Install test was completed on a physical device or emulator using the
  intended APK/AAB-derived artifact.

## Smoke Test Commands

Run the strongest practical local checks before handing off a public candidate:

```powershell
.\androidApp\gradlew.bat :app:processDebugResources --console=plain
.\androidApp\gradlew.bat :app:processReleaseResources --console=plain
.\androidApp\gradlew.bat lintDebug --console=plain
.\androidApp\gradlew.bat :app:testDebugUnitTest --console=plain
.\androidApp\gradlew.bat :app:assembleDebug --console=plain
.\androidApp\gradlew.bat :app:assembleRelease --console=plain
.\androidApp\gradlew.bat :app:bundleRelease --console=plain
```

Run connected checks when hardware is available:

```powershell
.\androidApp\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest --console=plain
```

## Distribution And Rollout

- [ ] Firebase/App Distribution internal tester flow was considered and kept
  separate from Play Store release artifacts.
- [ ] Play Console upload is manual or handled by a future approved workflow;
  this repository does not currently publish to Play Store.
- [ ] Staged rollout readiness, rollback plan, and release notes were reviewed.

## Play Store Assets And App Content

- [ ] `docs/release/play-store-assets.md` was reviewed.
- [ ] Store title, short description, full description, and release notes match
  implemented behavior and avoid unsupported banking, investment, tax, account,
  cloud backup, or AI claims.
- [ ] Launcher icon resources compile and final high-res icon artwork was
  reviewed before Play Console upload.
- [ ] Feature graphic and screenshots use deterministic sample-safe data only.
- [ ] Screenshots were captured from release or release-like builds and do not
  show Debug Health, stack traces, build metadata, real financial records,
  tester emails, Firebase IDs, or credentials.
- [ ] Play Console content rating, financial features, target audience, app
  access, ads, data safety, and permissions forms were completed from the final
  release artifact.

## Privacy, Observability, And Accessibility

- [ ] Privacy policy was created and reviewed against current app behavior.
- [ ] Privacy policy is published at a public HTTPS URL and that exact URL was
  entered in Play Console before public release.
- [ ] Public support contact is present in the privacy policy and Play Store
  listing. If no real public support email exists yet, release is blocked until
  one is approved.
- [ ] Privacy policy reflects Firebase Analytics, App Distribution, crash
  reporting, permissions, backup behavior, and local data storage accurately.
- [ ] Privacy policy does not claim unsupported privacy behavior and has a Play
  Store listing link/contact ready before submission.
- [ ] Privacy policy received final release/legal review before public Play Store
  publication.
- [ ] Local-first privacy claims match current behavior: no account login, no
  bank connection, no cloud sync, no remote AI path, no ads, and no sale of
  personal data by Pocket Ledger app code.
- [ ] `docs/play-store-readiness.md` was reviewed against the final release
  merged manifest and Play Console app content forms.
- [ ] Play Console Data Safety answers account for Firebase Analytics being
  present in the release app, even though Pocket Ledger product events are no-op
  in release and not wired to Firebase Analytics.
- [ ] Play Console permissions declarations use release merged-manifest
  permissions only and exclude debug/test-only permissions.
- [ ] Backup and device-transfer behavior was reviewed for #227, #7, #81, and
  #129 traceability.
- [ ] `backup_rules.xml` uses explicit deny-by-default rules and is not
  template-style.
- [ ] `data_extraction_rules.xml` defines both `cloud-backup` and
  `device-transfer` behavior.
- [ ] Caches, logs, temp files, debug artifacts, generated reports, external app
  files, local-only state, and device-protected storage are excluded.
- [ ] Ledger database files and SQLite sidecars are excluded unless a future
  backup-ready profile intentionally changes that policy.
- [ ] Encrypted/shared preferences are not accidentally exposed through backup
  or device transfer.
- [ ] Privacy policy and security model match the final merged manifest and XML
  backup/data-extraction behavior.
- [ ] Crash/observability configuration was reviewed.
- [ ] Product event taxonomy changes, if any, remain privacy-safe.
- [ ] Debug Health remains debug-only and is not routable in release builds.
- [ ] Release diagnostics do not expose sensitive data through UI,
  accessibility labels, logs, crash metadata, or artifacts.
- [ ] E-16 accessibility semantics, state descriptions, and 200% font scaling
  checks were preserved.
- [ ] AI/privacy behavior was reviewed against `docs/ai-architecture.md` and
  `docs/ai-privacy-safety-checklist.md`; current AI behavior remains local,
  on-device-provider-shell, no-op, or deterministic rule-based fallback only.
- [ ] No sensitive financial data, credentials, stack traces, Firebase tokens,
  Play Store credentials, or tester emails appear in logs or artifacts.

## Security And Backup-Ready Profile Review

- [ ] #7 sensitive local data criteria were reviewed against
  `androidApp/docs/security-model.md`.
- [ ] Sensitive preferences use `EncryptedSensitivePreferences` and AndroidX
  Security Crypto in normal app builds.
- [ ] Android Keystore-backed `MasterKey` behavior is documented and no custom
  raw secret storage was added.
- [ ] Optional app lock remains user-controlled and does not claim to encrypt
  the Room ledger database.
- [ ] #81 remains planned unless a real opt-in account/passkey/backend/recovery
  profile flow is implemented and tested.
- [ ] #227 deny-by-default backup and transfer policy remains correct until #81
  changes it through reviewed implementation.

## Performance And Accessibility Evidence

- [ ] Macrobenchmark and baseline-profile guidance in
  `androidApp/docs/performance-report.md` was reviewed.
- [ ] Connected benchmark/profile tasks were run on named hardware, or the
  release record explains why they were not run.
- [ ] Adaptive screenshot/font-scale evidence was reviewed when UI changes are
  included.
- [ ] Accessibility labels, headings, state descriptions, focus order, and large
  font behavior were checked for changed screens.

## Known Limitations

- [ ] No cloud sync, account/passkey flow, Play Integrity enforcement, import,
  export, OCR, bank connection, full database encryption, or remote AI behavior
  is claimed unless separately implemented and validated.
- [ ] Optional backup-ready profile behavior is documented in
  `docs/backup-ready-profile.md` and remains planned/partial.
- [ ] Binary Play Store screenshots and feature graphic uploads are manual unless
  committed and validated in a future asset pass.

## Final Release Gate

- [ ] README and release docs match the build commands used for the candidate.
- [ ] PR checklist items were completed or marked N/A with a reason.
- [ ] Known limitations were accepted by the release owner.
- [ ] Issue traceability is documented:
  - #129 release checklist completed/updated; manual release gates remain.
  - #131 store listing copy and asset plan prepared; Play Console upload and
    final binary graphics remain manual unless completed separately.
  - #132 privacy policy updated and ready for hosting/legal review.
  - #7 security criteria mapped to code/docs; remaining non-implemented items
    are explicit.
  - #81 backup-ready profile design documented; implementation remains future
    work until real opt-in profile behavior exists.
