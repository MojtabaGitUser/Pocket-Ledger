#!/usr/bin/env python3
"""Validate Firebase Android client configuration without printing secrets."""

from __future__ import annotations

import argparse
import json
from pathlib import Path


def validate_config(
    config_path: Path,
    expected_packages: set[str],
    expected_release_app_id: str | None,
) -> None:
    try:
        payload = json.loads(config_path.read_text(encoding="utf-8"))
    except FileNotFoundError as exc:
        raise ValueError(f"Firebase configuration is missing: {config_path}") from exc
    except json.JSONDecodeError as exc:
        raise ValueError("Firebase configuration is not valid JSON.") from exc

    clients = payload.get("client")
    if not isinstance(clients, list) or not clients:
        raise ValueError("Firebase configuration does not contain Android clients.")

    package_to_app_id: dict[str, str] = {}
    for client in clients:
        client_info = client.get("client_info", {}) if isinstance(client, dict) else {}
        android_info = client_info.get("android_client_info", {})
        package_name = android_info.get("package_name")
        app_id = client_info.get("mobilesdk_app_id")
        if isinstance(package_name, str) and isinstance(app_id, str):
            package_to_app_id[package_name] = app_id

    missing = expected_packages.difference(package_to_app_id)
    if missing:
        raise ValueError(
            "Firebase configuration is missing required Android package(s): "
            + ", ".join(sorted(missing))
        )

    if expected_release_app_id:
        configured_app_id = package_to_app_id.get("com.mojtaba.folentra")
        if configured_app_id != expected_release_app_id:
            raise ValueError(
                "FIREBASE_APP_ID does not match the com.mojtaba.folentra client."
            )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("config", type=Path)
    parser.add_argument(
        "--package",
        action="append",
        dest="packages",
        required=True,
        help="Required Android package name; repeat for multiple clients.",
    )
    parser.add_argument("--release-app-id")
    args = parser.parse_args()

    try:
        validate_config(
            config_path=args.config,
            expected_packages=set(args.packages),
            expected_release_app_id=args.release_app_id,
        )
    except ValueError as exc:
        parser.error(str(exc))

    print("Firebase Android client configuration is valid.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
