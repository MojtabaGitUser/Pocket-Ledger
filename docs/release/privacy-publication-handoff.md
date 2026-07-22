# Privacy Publication Handoff

This handoff separates repository-complete work from the owner-controlled values required for a public Play Store release.

## Repository deliverables

- `docs/privacy-policy.md` is the canonical detailed policy.
- `docs/privacy-policy.html` is a standalone responsive page for a static HTTPS host.
- `backup_rules.xml` denies app-private domains for pre-Android 12 backup.
- `data_extraction_rules.xml` applies the deny-by-default policy to Android 12+ cloud backup and device transfer.
- `validateBackupAndDeviceTransferRules` runs with Gradle `check` and fails if manifest references, extraction sections, or protected-domain exclusions are removed.

## Owner-supplied release values

1. Keep `support.folentra@gmail.com` monitored and protected with recovery options and two-factor authentication.
2. Host `docs/privacy-policy.html` at a stable public HTTPS URL.
3. Verify the URL signed out on desktop and mobile.
4. Put the same email and URL in Play Console.

## Verification

```powershell
.\gradlew.bat :app:validateBackupAndDeviceTransferRules
```

Before each release, inspect the merged release manifest and test a device migration. The expected result is a fresh empty ledger; transactions, budgets, tags, settings, App Lock, account-foundation state, and backup opt-in state must not migrate automatically.

Any future encrypted backup or restore must change the XML rules, validator, privacy policy, Data Safety answers, and restore documentation together.