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
