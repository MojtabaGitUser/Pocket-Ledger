# ADR 0003: Portfolio-Quality Documentation And Release Evidence

## Status

Accepted

## Context

Pocket Ledger is intended to be reviewed by humans as a portfolio artifact. Code alone is not enough for that review. A reviewer needs a fast path through the product story, architecture, test strategy, performance posture, release readiness, privacy/security boundaries, and honest limitations.

Without a traceable documentation set, the project risks looking like a collection of disconnected tasks instead of a coherent engineering sample.

## Decision

Pocket Ledger keeps a portfolio-facing documentation layer under `docs/portfolio/` and supports it with detailed engineering docs elsewhere in the repository.

The portfolio layer must include:

- A portfolio README for the project narrative and reviewer path.
- Architecture diagrams that summarize module and runtime structure.
- A testing and performance summary that links to detailed reports.
- A demo script with setup, sequence, and talking points.
- A traceability file mapping E-20 issues to concrete evidence.
- A final portfolio review documenting strengths and honest gaps.

Detailed docs such as testing reports, performance reports, release checklists, privacy policy, security model, and future extension plans remain in their existing locations.

## Consequences

Positive outcomes:

- Reviewers can understand the project quickly without reading every task history.
- Portfolio claims are backed by concrete files and commands.
- Known gaps are explicit and harder to accidentally overstate.
- Future documentation changes have a clear index and traceability point.

Tradeoffs:

- Documentation must be maintained when architecture, validation, or release workflows change.
- Some information is intentionally summarized in portfolio docs and detailed elsewhere, so links must stay current.

## Review Rules

- Portfolio docs must not claim features or validation results that are not implemented or documented.
- Device-required validation must be labeled as device-required.
- Play Store publication must not be claimed until it happens.
- Future features must remain in future-growth or extension-contract docs until implemented.