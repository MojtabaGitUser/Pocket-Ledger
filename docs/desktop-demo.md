# Desktop Demo

Pocket Ledger includes a Compose Multiplatform desktop demo in `:desktopApp`.
It is a JVM desktop target intended to demonstrate larger-screen presentation
of Pocket Ledger flows without changing Android app behavior.

## Run

From the repository root:

```powershell
.\androidApp\gradlew.bat :desktopApp:run --console=plain
```

From `androidApp/`:

```powershell
.\gradlew.bat :desktopApp:run --console=plain
```

## Insights Screen

Open the desktop app and choose `Insights` in the left navigation rail. The
screen uses sample-safe aggregate demo data, not local Room persistence and not
private user data. It does not call remote AI services.

The screen shows:

- Monthly summary for the sample demo period.
- Income, expense, and net overview.
- Top spending groups.
- Local insight cards and suggested actions.
- Provider status for deterministic local rule-based fallback behavior.
- Loading, empty, and error state rendering paths in the desktop UI model.

## Current Limitations

- The desktop demo is not wired to the Android Room database or repositories.
- Existing E-12 AI provider modules are Android-library modules, so the desktop
  target uses a JVM-compatible desktop insight mapper that follows the same
  aggregate-only, rule-based privacy behavior.
- Search and transaction desktop flows remain placeholders until their desktop
  tasks are implemented.
