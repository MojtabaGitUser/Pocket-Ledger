# Release Readiness Checklist

Use this checklist before treating a Pocket Ledger artifact as release-ready.
PR validation and release-candidate workflows support this process, but they do
not replace human review for signing, privacy, and Play Store readiness.

## Signing And Versioning

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

- [ ] Release candidate workflow completed successfully.
- [ ] Release APK and AAB artifacts were generated.
- [ ] R8 mapping artifacts were retained with the candidate record.
- [ ] `lintRelease`, JVM/shared tests, release assembly, bundle generation, and
  benchmark artifact assembly passed.
- [ ] Install test was completed on a physical device or emulator using the
  intended APK/AAB-derived artifact.

## Distribution And Rollout

- [ ] Firebase/App Distribution internal tester flow was considered and kept
  separate from Play Store release artifacts.
- [ ] Play Console upload is manual or handled by a future approved workflow;
  this repository does not currently publish to Play Store.
- [ ] Staged rollout readiness, rollback plan, and release notes were reviewed.

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
- [ ] `docs/play-store-readiness.md` was reviewed against the final release
  merged manifest and Play Console app content forms.
- [ ] Play Console Data Safety answers account for Firebase Analytics being
  present in the release app, even though Pocket Ledger product events are no-op
  in release and not wired to Firebase Analytics.
- [ ] Play Console permissions declarations use release merged-manifest
  permissions only and exclude debug/test-only permissions.
- [ ] Backup and device-transfer behavior was reviewed. If
  `backup_rules.xml` or `data_extraction_rules.xml` remain template-style, the
  release owner accepted that as a blocker or documented the final policy before
  submission.
- [ ] Crash/observability configuration was reviewed.
- [ ] Product event taxonomy changes, if any, remain privacy-safe.
- [ ] Debug Health remains debug-only and is not routable in release builds.
- [ ] Release diagnostics do not expose sensitive data through UI,
  accessibility labels, logs, crash metadata, or artifacts.
- [ ] E-16 accessibility semantics, state descriptions, and 200% font scaling
  checks were preserved.
- [ ] No sensitive financial data, credentials, stack traces, Firebase tokens,
  Play Store credentials, or tester emails appear in logs or artifacts.

## Final Review

- [ ] README and release docs match the build commands used for the candidate.
- [ ] PR checklist items were completed or marked N/A with a reason.
- [ ] Known limitations were accepted by the release owner.
