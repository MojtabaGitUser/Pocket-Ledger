# Optional Account And Passkey Foundation

This document records the implementation boundary for #83, #84, #85, #86, parent technical story #82, and user story #13.

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
| #13 Support optional passkey-enabled account flow | Complete for the local-first foundation scope | The app works without login, Settings exposes a disabled-by-default optional account profile entry, Credential Manager is behind `PasskeyClient`, passkey registration/authentication use challenge-response contracts, unsupported providers return safe unavailable/error states, and this document records the security assumptions. |
| #83 Add optional account settings entry | Complete | `SettingsScreen` shows an optional account profile row with local-first disabled default messaging. `OptionalAccountSettingsState` derives state from the passkey feature flag and provider availability. |
| #84 Define passkey API contract | Complete | `core/security/passkey/PasskeyModels.kt` and `PasskeyBackendContract.kt` define challenge-response contracts, client results, backend verification results, and unavailable/failure states. |
| #85 Implement Credential Manager prototype client | Complete | `AndroidCredentialManagerPasskeyClient` adapts AndroidX Credential Manager to the `PasskeyClient` contract and returns safe result states for cancellation, invalid request, provider failure, and unknown failure. |
| #86 Add Play Integrity request hook | Complete | `PlayIntegrityRequestHook` defines token request behavior; `AndroidPlayIntegrityRequestHook` adapts Google Play Integrity; `NoOpPlayIntegrityRequestHook` keeps disabled/default behavior explicit. |
| #82 Define optional passkey backend contract | Complete | The backend contract and no-op backend establish the challenge-response boundary needed before any real account service is introduced. |

## #13 Acceptance Criteria Mapping

| Acceptance criterion | Evidence | Status |
| --- | --- | --- |
| App works fully without login. | `DefaultFeatureFlags.PasskeyAccountFlowEnabled` defaults to false, `OptionalAccountSettingsState.LocalOnly` disables account controls, and app graph construction does not require a passkey backend. | Complete. |
| Optional account flow supports Credential Manager. | `AndroidCredentialManagerPasskeyClient` adapts AndroidX Credential Manager behind the `PasskeyClient` contract. | Complete for prototype/foundation scope. |
| Passkey flow follows challenge-response architecture. | `PasskeyBackendContract` separates begin/complete registration and authentication from the local Credential Manager client response. | Complete. |
| Unsupported flows fail gracefully. | `NoOpPasskeyClient`, `NoOpPasskeyBackendContract`, `NoOpPlayIntegrityRequestHook`, and typed unavailable/error result models keep missing providers and disabled features non-fatal. | Complete. |
| Local-first behavior remains default. | Optional account controls are off by default and do not gate ledger screens. | Complete. |

Authentication UI states are represented by `OptionalAccountAuthenticationUiState`:

- `LocalOnly`: default state; no login required and account controls are disabled.
- `Ready`: feature flag, passkey client, and Play Integrity request hook are available.
- `ReadyWithoutIntegrity`: passkey client is available but server-side Play Integrity enforcement is not configured.
- `ProviderUnavailable`: the build or device cannot start a passkey profile.

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
## 2026-07-20 Closure Validation For #13

The local-first foundation scope was re-audited against every acceptance criterion and task in #13. The app graph remains constructible without an account or backend; the feature flag defaults off; Credential Manager is isolated behind `PasskeyClient`; registration and authentication preserve the begin/complete challenge-response boundary; and unavailable providers, the no-op backend, and missing Play Integrity support return typed safe states.

The contract model now rejects contradictory availability values and blank safe backend failure messages. Tests exercise all four no-op backend operations: begin/complete registration and begin/complete authentication. This prevents future wiring from accidentally treating an unavailable backend as a partial success.

Validated commands:

```powershell
.\gradlew.bat :core:security:testDebugUnitTest --tests '*Passkey*'
.\gradlew.bat :app:testDebugUnitTest --tests '*OptionalAccount*'
.\gradlew.bat :app:assembleDebug :app:assembleRelease lintRelease
```

#13 is complete only for the documented foundation scope. Production registration, authentication, sessions, recovery, account deletion, and server-side verification remain deliberately excluded and require a separate backend epic.
