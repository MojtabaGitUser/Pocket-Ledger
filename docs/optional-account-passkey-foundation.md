# Optional Account And Passkey Foundation

This document records the implementation boundary for #83, #84, #85, #86, and parent technical story #82.

## Scope

Pocket Ledger remains fully local-first and fully usable without login. The optional account/passkey work in this implementation is a foundation and prototype layer only. It does not create a production account service, cloud backup, sync, recovery flow, or Play Integrity enforcement policy.

Implemented pieces:

- Optional account profile entry in Settings, gated by `DefaultFeatureFlags.PasskeyAccountFlowEnabled` and disabled by default.
- Passkey API contracts in `:core:security` for registration/authentication options, Credential Manager responses, client availability, backend challenge/verification flow, and safe error states.
- No-op backend contract that makes the absence of a server explicit and testable.
- Android Credential Manager prototype client in `:app` behind the `PasskeyClient` interface.
- Play Integrity request hook abstraction in `:core:security` with an Android implementation in `:app` and no-op fallback for tests/future disabled states.
- Unit tests for passkey contracts, no-op backend behavior, Play Integrity request validation, and Settings account state.

## Issue Traceability

| Issue | Status | Evidence |
| --- | --- | --- |
| #83 Add optional account settings entry | Complete | `SettingsScreen` shows an optional account profile row with local-first disabled default messaging. `OptionalAccountSettingsState` derives state from the passkey feature flag and provider availability. |
| #84 Define passkey API contract | Complete | `core/security/passkey/PasskeyModels.kt` and `PasskeyBackendContract.kt` define challenge-response contracts, client results, backend verification results, and unavailable/failure states. |
| #85 Implement Credential Manager prototype client | Complete | `AndroidCredentialManagerPasskeyClient` adapts AndroidX Credential Manager to the `PasskeyClient` contract and returns safe result states for cancellation, invalid request, provider failure, and unknown failure. |
| #86 Add Play Integrity request hook | Complete | `PlayIntegrityRequestHook` defines token request behavior; `AndroidPlayIntegrityRequestHook` adapts Google Play Integrity; `NoOpPlayIntegrityRequestHook` keeps disabled/default behavior explicit. |
| #82 Define optional passkey backend contract | Complete | The backend contract and no-op backend establish the challenge-response boundary needed before any real account service is introduced. |

## Architecture Boundary

```text
Settings UI
    -> OptionalAccountSettingsState
        -> FeatureFlagEvaluator(DefaultFeatureFlags.PasskeyAccountFlowEnabled)
        -> PasskeyClient.availability()
        -> PlayIntegrityRequestHook.availability()

Future account flow
    -> PasskeyBackendContract.beginRegistration / beginAuthentication
    -> AndroidCredentialManagerPasskeyClient.register / authenticate
    -> PasskeyBackendContract.completeRegistration / completeAuthentication
    -> optional PlayIntegrityRequestHook.requestToken
```

## Security Rules

- Do not store passkey credentials or account session tokens outside `SensitivePreferences`.
- Do not log Credential Manager request JSON, response JSON, Play Integrity tokens, account identifiers, session tokens, challenges, or raw backend payloads.
- Keep the feature flag disabled by default until a real backend, recovery model, privacy review, and release review exist.
- Treat Play Integrity as a request signal for a backend decision, not as local-only proof of user identity.
- Keep Pocket Ledger usable without login.

## Explicit Non-Goals

Not implemented by this task batch:

- Production backend account service.
- Real cloud backup or sync.
- Account recovery, deletion, or support workflow.
- Server-side Play Integrity verdict verification.
- Play Store or backend configuration for production enforcement.
- Inclusion of ledger data in Android backup/device transfer.

## Validation

Run from the repository root:

```powershell
.\androidApp\gradlew.bat --no-daemon :core:security:testDebugUnitTest :app:testDebugUnitTest :app:compileDebugKotlin :app:compileReleaseKotlin --console=plain --stacktrace
```