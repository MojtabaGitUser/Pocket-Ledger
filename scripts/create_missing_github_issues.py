#!/usr/bin/env python3
"""Idempotently create missing Pocket Ledger GitHub issues.

Source: Pocket_Ledger_Complete_Backlog.docx, table 92.
This file intentionally embeds every missing non-epic backlog issue detected on
2026-05-16. It does not require the DOCX at runtime.

Usage:
  $env:GITHUB_TOKEN = "ghp_..."  # PowerShell
  python scripts/create_missing_github_issues.py

The script:
- reads GITHUB_TOKEN from the environment
- fetches existing issues from MojtabaGitUser/Pocket-Ledger
- compares by normalized title and backlog ID
- creates only missing Story, Tech Story, and Task issues
- updates parent Story/Tech Story issue bodies with linked child task checklists
"""

from __future__ import annotations

import os
import re
import sys
import time
from dataclasses import dataclass
from typing import Any

import requests

OWNER = "MojtabaGitUser"
REPO = "Pocket-Ledger"
API = f"https://api.github.com/repos/{OWNER}/{REPO}"

MILESTONE_BY_EPIC = {
    "E-00": "Week 1 - Foundation",
    "E-01": "Week 1 - Foundation",
    "E-02": "Week 2 - Offline-first + Adaptive UI",
    "E-03": "Week 2 - Offline-first + Adaptive UI",
    "E-04": "Week 2 - Offline-first + Adaptive UI",
    "E-05": "Week 2 - Offline-first + Adaptive UI",
    "E-06": "Week 2 - Offline-first + Adaptive UI",
    "E-07": "Week 2 - Offline-first + Adaptive UI",
    "E-08": "Week 2 - Offline-first + Adaptive UI",
    "E-09": "Week 2 - Offline-first + Adaptive UI",
    "E-10": "Week 3 - AI + Security + Testing",
    "E-11": "Week 3 - AI + Security + Testing",
    "E-12": "Week 3 - AI + Security + Testing",
    "E-13": "Week 3 - AI + Security + Testing",
    "E-14": "Week 3 - AI + Security + Testing",
    "E-15": "Week 4 - Performance + Release Hardening",
    "E-16": "Week 3 - AI + Security + Testing",
    "E-17": "Week 4 - Performance + Release Hardening",
    "E-18": "Week 4 - Performance + Release Hardening",
    "E-19": "Week 4 - Performance + Release Hardening",
    "E-20": "Week 4 - Performance + Release Hardening",
    "E-21": "Week 4 - Performance + Release Hardening",
}

EPIC_TITLES = {
    "E-00": "E-00 - Project Foundation & Modular Architecture",
    "E-01": "E-01 - Design System & Navigation",
    "E-02": "E-02 - Transaction Management",
    "E-03": "E-03 - Dashboard, Budgets & Non-AI Insights",
    "E-04": "E-04 - Offline-First Data Layer",
    "E-05": "E-05 - Search & Filtering",
    "E-06": "E-06 - Adaptive UI for Phone, Tablet, Foldable & Desktop Mode",
    "E-07": "E-07 - KMP Shared Module",
    "E-08": "E-08 - Compose Multiplatform Desktop Demo",
    "E-09": "E-09 - WorkManager & Background Jobs",
    "E-10": "E-10 - Security & Privacy",
    "E-11": "E-11 - Optional Account / Passkey Flow",
    "E-12": "E-12 - On-device AI & Fallback Logic",
    "E-13": "E-13 - Feature Flags & Modular Delivery",
    "E-14": "E-14 - Testing Strategy",
    "E-15": "E-15 - Performance Engineering",
    "E-16": "E-16 - Accessibility",
    "E-17": "E-17 - Observability & Crash Reporting",
    "E-18": "E-18 - CI/CD",
    "E-19": "E-19 - Play Store Readiness",
    "E-20": "E-20 - Documentation & Portfolio Presentation",
    "E-21": "E-21 - Future Phase / Monetization Readiness",
}

AREA_BY_EPIC = {
    "E-00": "area:project-foundation",
    "E-01": "area:design-system",
    "E-02": "area:transaction-management",
    "E-03": "area:dashboard",
    "E-04": "area:offline-first-data-layer",
    "E-05": "area:search-filtering",
    "E-06": "area:adaptive-ui",
    "E-07": "area:kmp-shared",
    "E-08": "area:desktop-demo",
    "E-09": "area:workmanager",
    "E-10": "area:security-privacy",
    "E-11": "area:optional-account",
    "E-12": "area:ai-fallback",
    "E-13": "area:feature-flags",
    "E-14": "area:testing",
    "E-15": "area:performance",
    "E-16": "area:accessibility",
    "E-17": "area:observability",
    "E-18": "area:ci-cd",
    "E-19": "area:play-store-readiness",
    "E-20": "area:documentation",
    "E-21": "area:future-phase---monetization-readiness",
}

PRIORITY_BY_EPIC = {
    "E-03": "must",
    "E-08": "should",
    "E-09": "should",
    "E-11": "could",
    "E-12": "should",
    "E-13": "should",
    "E-17": "should",
    "E-21": "could",
}

# Every missing Story, Technical Story, Task, and Subtask detected from the DOCX.
# Epic rows are intentionally excluded.
BACKLOG_ISSUES = [
    "[Technical Story] TS-E00-01 - Establish multi-module Gradle architecture",
    "[Task] T-E00-03 - Define architecture package and module rules",
    "[Task] T-E00-04 - Configure debug/release variants",
    "[Technical Story] TS-E01-01 - Create shared Compose design system",
    "[Task] T-E01-02 - Create shared UI components",
    "[Task] T-E01-04 - Add design preview fixtures",
    "[Story] US-E02-02 - View and manage transaction details",
    "[Technical Story] TS-E02-01 - Create transaction domain model and validation",
    "[Task] T-E02-01 - Implement transaction form state and validation",
    "[Task] T-E02-04 - Implement delete and undo/confirmation",
    "[Task] T-E02-05 - Add transaction tests",
    "[Story] US-E03-02 - Set and view simple budget status",
    "[Technical Story] TS-E03-01 - Implement aggregation and budget status use cases",
    "[Task] T-E03-01 - Create dashboard summary models",
    "[Task] T-E03-03 - Build Dashboard Compose screen",
    "[Task] T-E03-04 - Implement simple budget setup",
    "[Task] T-E03-05 - Add dashboard tests",
    "[Technical Story] TS-E04-01 - Implement Room KMP local database",
    "[Task] T-E04-02 - Implement DAOs and repositories",
    "[Task] T-E04-03 - Add database migrations and tests",
    "[Task] T-E04-04 - Add seed/demo data tools",
    "[Task] T-E04-05 - Create offline-first repository contract",
    "[Technical Story] TS-E05-01 - Create shared search model and ranking rules",
    "[Task] T-E05-01 - Define SearchQuery and filter models",
    "[Task] T-E05-02 - Implement indexed keyword search",
    "[Task] T-E05-04 - Connect search to semantic mode placeholder",
    "[Technical Story] TS-E06-01 - Create adaptive layout infrastructure",
    "[Task] T-E06-01 - Create adaptive navigation shell",
    "[Task] T-E06-02 - Implement transaction list/detail adaptive layout",
    "[Task] T-E06-03 - Adapt dashboard layout",
    "[Task] T-E06-04 - Add adaptive screenshot test matrix",
    "[Technical Story] TS-E07-01 - Move stable business logic to shared KMP",
    "[Task] T-E07-02 - Move domain models and validation",
    "[Task] T-E07-03 - Share search and aggregation rules",
    "[Task] T-E07-04 - Document KMP boundaries",
    "[Technical Story] TS-E08-01 - Create desktop target consuming shared logic",
    "[Task] T-E08-01 - Add desktop demo module",
    "[Task] T-E08-02 - Implement desktop Search screen",
    "[Task] T-E08-03 - Implement desktop Insights screen",
    "[Task] T-E08-04 - Document desktop demo limitations",
    "[Technical Story] TS-E09-01 - Create background job infrastructure",
    "[Task] T-E09-01 - Create WorkManager scheduler abstraction",
    "[Task] T-E09-02 - Implement monthly summary preparation worker",
    "[Task] T-E09-03 - Implement reminder scheduling settings",
    "[Task] T-E09-04 - Expose worker status in Debug Health",
    "[Technical Story] TS-E10-01 - Implement privacy-safe local security layer",
    "[Task] T-E10-02 - Implement encrypted sensitive preferences",
    "[Task] T-E10-03 - Implement optional app lock",
    "[Task] T-E10-04 - Add privacy-safe logging policy",
    "[Task] T-E10-05 - Document security model and limitations",
    "[Story] US-E11-01 - Create an optional backup-ready profile",
    "[Technical Story] TS-E11-01 - Define optional passkey backend contract",
    "[Task] T-E11-01 - Add optional account settings entry",
    "[Task] T-E11-02 - Define passkey API contract",
    "[Task] T-E11-03 - Implement Credential Manager prototype client",
    "[Task] T-E11-04 - Add Play Integrity request hook",
    "[Story] US-E12-01 - Generate a private monthly summary",
    "[Story] US-E12-02 - Use semantic search and smart autofill",
    "[Technical Story] TS-E12-01 - Create AI provider abstraction with fallback",
    "[Task] T-E12-01 - Define AI feature contracts",
    "[Task] T-E12-02 - Implement rule-based fallback provider",
    "[Task] T-E12-03 - Integrate on-device AI provider",
    "[Task] T-E12-04 - Build Insights screen",
    "[Task] T-E12-05 - Add AI privacy and safety tests/checklist",
    "[Technical Story] TS-E13-01 - Implement feature flag provider abstraction",
    "[Task] T-E13-01 - Define typed feature flags",
    "[Task] T-E13-02 - Implement local JSON/default provider",
    "[Task] T-E13-03 - Add debug flag override screen",
    "[Task] T-E13-04 - Document Play Feature Delivery strategy",
    "[Technical Story] TS-E14-01 - Implement layered testing framework",
    "[Task] T-E14-01 - Create core-testing module",
    "[Task] T-E14-02 - Add shared unit tests",
    "[Task] T-E14-03 - Add Room and repository integration tests",
    "[Task] T-E14-04 - Add Compose UI tests for critical flows",
    "[Task] T-E14-05 - Add screenshot test matrix",
    "[Task] T-E14-06 - Create testing report",
    "[Technical Story] TS-E15-01 - Benchmark startup and scrolling performance",
    "[Task] T-E15-01 - Set up Macrobenchmark module",
    "[Task] T-E15-02 - Set up Baseline Profile generation",
    "[Task] T-E15-03 - Add large dataset performance scenario",
    "[Task] T-E15-04 - Run recomposition and jank review",
    "[Task] T-E15-05 - Tune release build and R8",
    "[Task] T-E15-06 - Add LeakCanary/profiler pass in debug",
    "[Technical Story] TS-E16-01 - Add accessibility testing and checklist",
    "[Task] T-E16-01 - Audit semantics for primary screens",
    "[Task] T-E16-02 - Add semantic labels and state descriptions",
    "[Task] T-E16-03 - Test 200% font scaling",
    "[Task] T-E16-04 - Add accessibility checks to PR template",
    "[Technical Story] TS-E17-01 - Implement privacy-safe observability",
    "[Task] T-E17-02 - Configure App Distribution/internal tester flow",
    "[Task] T-E17-03 - Implement Debug Health screen",
    "[Task] T-E17-04 - Define product event taxonomy",
    "[Technical Story] TS-E18-01 - Create GitHub Actions delivery pipeline",
    "[Task] T-E18-01 - Add PR validation workflow",
    "[Task] T-E18-02 - Add release candidate workflow",
    "[Task] T-E18-03 - Add screenshot/benchmark workflow strategy",
    "[Task] T-E18-04 - Publish CI badges and commands",
    "[Story] US-E19-01 - Install a release-ready Pocket Ledger build",
    "[Technical Story] TS-E19-01 - Create release hardening checklist",
    "[Task] T-E19-01 - Create release signing and versioning plan",
    "[Task] T-E19-02 - Prepare Play Store assets",
    "[Task] T-E19-03 - Write privacy policy",
    "[Task] T-E19-04 - Complete app content checklist",
    "[Task] T-E19-05 - Run release candidate smoke test",
    "[Story] US-E20-01 - Review the project as a portfolio artifact",
    "[Technical Story] TS-E20-01 - Create traceable documentation set",
    "[Task] T-E20-01 - Write portfolio README",
    "[Task] T-E20-02 - Create architecture diagram and ADRs",
    "[Task] T-E20-03 - Create testing and performance reports",
    "[Task] T-E20-04 - Create portfolio demo script",
    "[Story] US-E21-01 - Plan future product growth",
    "[Technical Story] TS-E21-01 - Define future extension contracts without implementing them",
    "[Task] T-E21-01 - Document cloud sync extension path",
    "[Task] T-E21-02 - Document OCR and receipt import path",
    "[Task] T-E21-03 - Document export and accountant/freelancer path",
    "[Task] T-E21-04 - Document monetization/entitlement path",
]

assert len(BACKLOG_ISSUES) == 116, f"Expected 116 missing issues, got {len(BACKLOG_ISSUES)}"


@dataclass(frozen=True)
class IssueSpec:
    title: str
    ident: str
    parent_epic: str
    parent_id: str | None
    labels: list[str]
    milestone_name: str
    priority: str
    description: str

    def body(self, child_tasks: list[tuple[int, str]] | None = None) -> str:
        lines = [
            f"## Parent Epic\n{self.parent_epic}",
        ]
        if self.parent_id:
            lines.append(f"## Parent Story or Parent Task\n{self.parent_id}")
        lines.extend([
            f"## Description\n{self.description}",
            "## Acceptance Criteria / Definition of Done\nComplete the described work and satisfy the acceptance criteria or Definition of Done from `Pocket_Ledger_Complete_Backlog.docx`.",
            f"## Suggested labels\n{', '.join(self.labels)}",
            f"## Suggested milestone\n{self.milestone_name}",
            f"## Priority\n{self.priority}",
            "## Related files/modules\nNot specified in the backlog import summary table. See the source DOCX detailed backlog tables for likely files/modules where applicable.",
        ])
        if child_tasks:
            lines.append("## Child Task Checklist")
            lines.extend(f"- [ ] #{number} - {title}" for number, title in child_tasks)
        return "\n\n".join(lines) + "\n"


def normalize_title(title: str) -> str:
    return re.sub(r"\s+", " ", title.strip().lower())


def extract_id(title: str) -> str:
    match = re.search(r"((?:US|TS|T)-E\d{2}-\d{2})", title)
    if not match:
        raise ValueError(f"No backlog ID found in title: {title}")
    return match.group(1)


def epic_from_id(ident: str) -> str:
    match = re.search(r"E\d{2}", ident)
    if not match:
        raise ValueError(f"No epic ID found in backlog ID: {ident}")
    return match.group(0).replace("E", "E-")


def issue_kind(title: str) -> str:
    if title.startswith("[Story]"):
        return "story"
    if title.startswith("[Technical Story]"):
        return "technical-story"
    return "task"


def title_summary(title: str) -> str:
    return title.split(" - ", 1)[1] if " - " in title else title


def build_specs() -> list[IssueSpec]:
    specs: list[IssueSpec] = []
    current_parent_by_epic: dict[str, str] = {}
    for title in BACKLOG_ISSUES:
        ident = extract_id(title)
        epic = epic_from_id(ident)
        kind = issue_kind(title)
        parent_id = None if kind in {"story", "technical-story"} else current_parent_by_epic.get(epic)
        if kind in {"story", "technical-story"}:
            current_parent_by_epic[epic] = ident
        labels = [f"type:{kind}", AREA_BY_EPIC[epic], f"priority:{PRIORITY_BY_EPIC.get(epic, 'must')}"]
        specs.append(
            IssueSpec(
                title=title,
                ident=ident,
                parent_epic=EPIC_TITLES[epic],
                parent_id=parent_id,
                labels=labels,
                milestone_name=MILESTONE_BY_EPIC[epic],
                priority=PRIORITY_BY_EPIC.get(epic, "must"),
                description=title_summary(title),
            )
        )
    return specs


def github_request(method: str, path: str, token: str, **kwargs: Any) -> Any:
    headers = {
        "Accept": "application/vnd.github+json",
        "Authorization": f"Bearer {token}",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "pocket-ledger-issue-importer",
    }
    response = requests.request(method, f"{API}{path}", headers=headers, timeout=30, **kwargs)
    if response.status_code == 403 and response.headers.get("X-RateLimit-Remaining") == "0":
        reset_at = int(response.headers.get("X-RateLimit-Reset", "0"))
        time.sleep(max(1, reset_at - int(time.time()) + 1))
        response = requests.request(method, f"{API}{path}", headers=headers, timeout=30, **kwargs)
    response.raise_for_status()
    return response.json() if response.content else None


def fetch_existing_issues(token: str) -> list[dict[str, Any]]:
    issues: list[dict[str, Any]] = []
    page = 1
    while True:
        batch = github_request("GET", f"/issues?state=all&per_page=100&page={page}", token)
        if not batch:
            break
        issues.extend(item for item in batch if "pull_request" not in item)
        if len(batch) < 100:
            break
        page += 1
    return issues


def fetch_milestones(token: str) -> dict[str, int]:
    milestones: dict[str, int] = {}
    page = 1
    while True:
        batch = github_request("GET", f"/milestones?state=all&per_page=100&page={page}", token)
        if not batch:
            break
        for milestone in batch:
            milestones[milestone["title"]] = milestone["number"]
        if len(batch) < 100:
            break
        page += 1
    return milestones


def create_issue(token: str, spec: IssueSpec, milestone_number: int | None) -> dict[str, Any]:
    payload: dict[str, Any] = {
        "title": spec.title,
        "body": spec.body(),
    }
    if milestone_number is not None:
        payload["milestone"] = milestone_number
    return github_request("POST", "/issues", token, json=payload)


def update_issue_body(token: str, issue_number: int, body: str) -> dict[str, Any]:
    return github_request("PATCH", f"/issues/{issue_number}", token, json={"body": body})


def main() -> int:
    token = os.environ.get("GITHUB_TOKEN")
    if not token:
        print("GITHUB_TOKEN is required. No GitHub issues were created.", file=sys.stderr)
        return 2

    specs = build_specs()
    existing = fetch_existing_issues(token)
    milestones = fetch_milestones(token)

    issue_by_normalized_title = {normalize_title(issue["title"]): issue for issue in existing}
    issue_by_id = {
        match.group(1): issue
        for issue in existing
        if (match := re.search(r"((?:US|TS|T)-E\d{2}-\d{2})", issue["title"]))
    }

    created: list[dict[str, Any]] = []
    skipped: list[str] = []
    for spec in specs:
        if normalize_title(spec.title) in issue_by_normalized_title or spec.ident in issue_by_id:
            skipped.append(spec.title)
            continue
        milestone_number = milestones.get(spec.milestone_name)
        issue = create_issue(token, spec, milestone_number)
        created.append(issue)
        issue_by_normalized_title[normalize_title(issue["title"])] = issue
        issue_by_id[spec.ident] = issue
        print(f"created #{issue['number']} {spec.title}")

    child_tasks_by_parent: dict[str, list[tuple[int, str]]] = {}
    for spec in specs:
        if not spec.title.startswith("[Task]") or not spec.parent_id:
            continue
        issue = issue_by_id.get(spec.ident)
        if issue:
            child_tasks_by_parent.setdefault(spec.parent_id, []).append((issue["number"], spec.title))

    updated_parents: list[str] = []
    for parent_id, child_tasks in child_tasks_by_parent.items():
        parent_issue = issue_by_id.get(parent_id)
        parent_spec = next((spec for spec in specs if spec.ident == parent_id), None)
        if not parent_issue or not parent_spec:
            continue
        update_issue_body(token, parent_issue["number"], parent_spec.body(sorted(child_tasks)))
        updated_parents.append(parent_id)
        print(f"updated parent #{parent_issue['number']} {parent_id}")

    print("summary:")
    print(f"  existing issues fetched: {len(existing)}")
    print(f"  backlog missing specs embedded: {len(specs)}")
    print(f"  skipped as already present: {len(skipped)}")
    print(f"  created: {len(created)}")
    print(f"  parent bodies updated: {len(updated_parents)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
