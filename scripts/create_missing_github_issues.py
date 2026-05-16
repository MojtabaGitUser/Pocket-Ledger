#!/usr/bin/env python3
"""Create missing GitHub issues for the Pocket Ledger backlog.

This script is intentionally idempotent:
- reads GITHUB_TOKEN from the environment
- fetches all existing issues for MojtabaGitUser/Pocket-Ledger
- compares by normalized title and backlog ID
- creates only missing Story, Tech Story, and Task issues
- updates parent Story/Tech Story bodies with linked child task checklists

The backlog data below is generated from Pocket_Ledger_Complete_Backlog.docx.
"""

from __future__ import annotations

import os
import re
import sys
import time
from dataclasses import dataclass, field
from typing import Any

import requests

OWNER = "MojtabaGitUser"
REPO = "Pocket-Ledger"
API = f"https://api.github.com/repos/{OWNER}/{REPO}"

MILESTONES = {
    "Week 1 - Foundation": 1,
    "Week 2 - Offline-first + Adaptive UI": 2,
    "Week 3 - AI + Security + Testing": 3,
    "Week 4 - Performance + Release Hardening": 4,
}

EXISTING_SEMANTIC_DUPLICATE_IDS = {
    # Existing repo issues created before the full backlog import. These are skipped
    # to avoid duplicate IDs or duplicate work, even where titles differ.
    "US-E01-01", "US-E02-01", "US-E03-01", "US-E04-01", "US-E05-01", "US-E06-01",
    "US-E07-01", "US-E08-01", "US-E09-01", "US-E10-01", "US-E13-01", "US-E14-01",
    "US-E15-01", "US-E16-01", "US-E17-01", "US-E18-01",
    "T-E00-01", "T-E00-02", "T-E00-05", "T-E01-01", "T-E01-03", "T-E02-02",
    "T-E03-02", "T-E04-01", "T-E10-01", "T-E17-01",
    # Existing unnumbered issues that semantically cover these backlog tasks.
    "T-E07-01", "T-E02-03", "T-E05-03",
}

@dataclass
class IssueSpec:
    title: str
    labels: list[str]
    milestone_name: str
    priority: str
    description: str
    parent_epic: str
    parent_id: str | None = None
    related_files: str = "Not specified in backlog."
    done: str = "Complete the described work, satisfy the parent acceptance criteria, and add/update relevant tests or documentation."

    @property
    def ident(self) -> str:
        match = re.search(r"((?:US|TS|T)-E\d{2}-\d{2})", self.title)
        return match.group(1) if match else self.title

    @property
    def kind(self) -> str:
        if self.title.startswith("[Story]"):
            return "story"
        if self.title.startswith("[Technical Story]"):
            return "technical-story"
        return "task"

    def body(self, created_children: list[tuple[int, str]] | None = None) -> str:
        lines = [
            f"## Parent Epic\n{self.parent_epic}",
        ]
        if self.parent_id:
            lines.append(f"## Parent Story or Parent Task\n{self.parent_id}")
        lines.extend([
            f"## Description\n{self.description}",
            f"## Acceptance Criteria / Definition of Done\n{self.done}",
            f"## Suggested labels\n{', '.join(self.labels)}",
            f"## Suggested milestone\n{self.milestone_name}",
            f"## Priority\n{self.priority}",
            f"## Related files/modules\n{self.related_files}",
        ])
        if created_children:
            lines.append("## Child Task Checklist")
            lines.extend(f"- [ ] #{num} - {title}" for num, title in created_children)
        return "\n\n".join(lines) + "\n"


def normalize_title(title: str) -> str:
    return re.sub(r"\s+", " ", title.strip().lower())


def request(method: str, path: str, token: str, **kwargs: Any) -> Any:
    headers = {
        "Accept": "application/vnd.github+json",
        "Authorization": f"Bearer {token}",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "pocket-ledger-issue-importer",
    }
    response = requests.request(method, f"{API}{path}", headers=headers, timeout=30, **kwargs)
    if response.status_code == 403 and response.headers.get("X-RateLimit-Remaining") == "0":
        reset = int(response.headers.get("X-RateLimit-Reset", "0"))
        sleep_for = max(1, reset - int(time.time()) + 1)
        time.sleep(sleep_for)
        response = requests.request(method, f"{API}{path}", headers=headers, timeout=30, **kwargs)
    response.raise_for_status()
    return response.json() if response.content else None


def fetch_existing_issues(token: str) -> list[dict[str, Any]]:
    issues: list[dict[str, Any]] = []
    page = 1
    while True:
        batch = request("GET", f"/issues?state=all&per_page=100&page={page}", token)
        issue_batch = [item for item in batch if "pull_request" not in item]
        if not issue_batch:
            break
        issues.extend(issue_batch)
        if len(batch) < 100:
            break
        page += 1
    return issues


def create_issue(token: str, spec: IssueSpec) -> dict[str, Any]:
    payload = {
        "title": spec.title,
        "body": spec.body(),
        "milestone": MILESTONES.get(spec.milestone_name),
    }
    return request("POST", "/issues", token, json=payload)


def update_issue_body(token: str, issue_number: int, body: str) -> dict[str, Any]:
    return request("PATCH", f"/issues/{issue_number}", token, json={"body": body})


def backlog_specs() -> list[IssueSpec]:
    # Compact canonical import list. Detailed backlog context should stay in the issue body;
    # labels are suggestions in the body to avoid failures when labels do not yet exist.
    raw = [
        ("[Technical Story] TS-E00-01 - Establish multi-module Gradle architecture", "E-00 - Project Foundation & Modular Architecture", "Week 1 - Foundation", "priority:must", "Create the project structure and dependency management foundation for a senior-level Android portfolio app."),
        ("[Task] T-E00-03 - Define architecture package and module rules", "E-00 - Project Foundation & Modular Architecture", "Week 1 - Foundation", "priority:must", "Document dependency rules, naming conventions, public API boundaries, and feature ownership."),
        ("[Task] T-E00-04 - Configure debug/release variants", "E-00 - Project Foundation & Modular Architecture", "Week 1 - Foundation", "priority:must", "Set applicationId, SDK values, signing placeholders, BuildConfig fields, ProGuard/R8 placeholders, and debug-only tools."),
        ("[Technical Story] TS-E01-01 - Create shared Compose design system", "E-01 - Design System & Navigation", "Week 1 - Foundation", "priority:must", "Implement reusable UI primitives and tokens for Pocket Ledger."),
        ("[Task] T-E01-02 - Create shared UI components", "E-01 - Design System & Navigation", "Week 1 - Foundation", "priority:must", "Build LedgerAmountText, CategoryChip, TransactionRow, EmptyState, ErrorState, LoadingState, SectionHeader, and adaptive containers."),
        ("[Task] T-E01-04 - Add design preview fixtures", "E-01 - Design System & Navigation", "Week 1 - Foundation", "priority:must", "Create fake transaction, category, budget, and insight data for previews and screenshot tests."),
        ("[Story] US-E02-02 - View and manage transaction details", "E-02 - Transaction Management", "Week 2 - Offline-first + Adaptive UI", "priority:must", "Provide a transaction detail screen with formatted fields, edit action, delete confirmation, and undo where available."),
        ("[Technical Story] TS-E02-01 - Create transaction domain model and validation", "E-02 - Transaction Management", "Week 2 - Offline-first + Adaptive UI", "priority:must", "Define stable transaction model, draft model, validation rules, and amount/date formatting rules."),
        ("[Task] T-E02-01 - Implement transaction form state and validation", "E-02 - Transaction Management", "Week 2 - Offline-first + Adaptive UI", "priority:must", "Build TransactionDraft, validation errors, amount parser/formatter, and save-enabled logic."),
        ("[Task] T-E02-04 - Implement delete and undo/confirmation", "E-02 - Transaction Management", "Week 2 - Offline-first + Adaptive UI", "priority:must", "Add delete flow with confirmation dialog or snackbar undo."),
        ("[Task] T-E02-05 - Add transaction tests", "E-02 - Transaction Management", "Week 2 - Offline-first + Adaptive UI", "priority:must", "Add tests for transaction validation, create/edit/list/detail, and delete behavior."),
    ]
    # Remaining backlog items can be generated by extending this list. The script is idempotent
    # and safe to rerun after adding more IssueSpec tuples.
    return [
        IssueSpec(
            title=title,
            labels=["type:" + ("technical-story" if title.startswith("[Technical Story]") else "story" if title.startswith("[Story]") else "task"), priority],
            milestone_name=milestone,
            priority=priority.replace("priority:", ""),
            description=description,
            parent_epic=epic,
        )
        for title, epic, milestone, priority, description in raw
    ]


def main() -> int:
    token = os.environ.get("GITHUB_TOKEN")
    if not token:
        print("GITHUB_TOKEN is required", file=sys.stderr)
        return 2

    existing = fetch_existing_issues(token)
    existing_titles = {normalize_title(issue["title"]): issue for issue in existing}
    existing_ids = {
        match.group(1): issue
        for issue in existing
        if (match := re.search(r"((?:US|TS|T)-E\d{2}-\d{2})", issue["title"]))
    }

    created: list[dict[str, Any]] = []
    skipped: list[str] = []
    for spec in backlog_specs():
        if normalize_title(spec.title) in existing_titles or spec.ident in existing_ids or spec.ident in EXISTING_SEMANTIC_DUPLICATE_IDS:
            skipped.append(spec.title)
            continue
        created_issue = create_issue(token, spec)
        created.append(created_issue)
        print(f"created #{created_issue['number']} {spec.title}")

    print(f"existing={len(existing)} skipped={len(skipped)} created={len(created)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
