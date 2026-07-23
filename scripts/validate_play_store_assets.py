#!/usr/bin/env python3
"""Validate committed Play Store PNGs without third-party dependencies."""
from __future__ import annotations
import struct
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSET_ROOT = ROOT / "docs" / "release" / "assets" / "play-store"
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"

def png_metadata(path: Path) -> tuple[int, int, int]:
    data = path.read_bytes()
    if data[:8] != PNG_SIGNATURE or data[12:16] != b"IHDR":
        raise ValueError("not a valid PNG")
    width, height, bit_depth, color_type = struct.unpack(">IIBB", data[16:26])
    if bit_depth != 8:
        raise ValueError(f"expected 8-bit PNG, found {bit_depth}-bit")
    return width, height, color_type

def validate_exact(path: Path, expected: tuple[int, int], opaque: bool = False) -> None:
    width, height, color_type = png_metadata(path)
    if (width, height) != expected:
        raise ValueError(f"expected {expected[0]}x{expected[1]}, found {width}x{height}")
    if opaque and color_type in (4, 6):
        raise ValueError("must not contain an alpha channel")

def validate_screenshot(path: Path, kind: str) -> None:
    width, height, _ = png_metadata(path)
    shortest, longest = sorted((width, height))
    if shortest < 320 or longest > 3840:
        raise ValueError(f"{kind} dimensions {width}x{height} are outside 320..3840")
    if longest / shortest > 2.3:
        raise ValueError(f"{kind} aspect ratio {longest / shortest:.2f} is too extreme")

def main() -> int:
    errors: list[str] = []
    phone = sorted((ASSET_ROOT / "phone").glob("*.png"))
    tablet = sorted((ASSET_ROOT / "tablet").glob("*.png"))
    checks = [
        (ASSET_ROOT / "icon-512.png", lambda p: validate_exact(p, (512, 512), opaque=True)),
        (ASSET_ROOT / "feature-graphic.png", lambda p: validate_exact(p, (1024, 500), opaque=True)),
        *((path, lambda p: validate_screenshot(p, "phone screenshot")) for path in phone),
        *((path, lambda p: validate_screenshot(p, "tablet screenshot")) for path in tablet),
    ]
    if len(phone) < 4: errors.append("phone: expected at least four screenshots")
    if len(tablet) < 3: errors.append("tablet: expected at least three screenshots")
    for path, validator in checks:
        try:
            if not path.is_file(): raise ValueError("file is missing")
            validator(path)
            print(f"OK {path.relative_to(ROOT)}")
        except (OSError, ValueError) as error:
            errors.append(f"{path.relative_to(ROOT)}: {error}")
    if errors:
        for error in errors: print(f"ERROR {error}", file=sys.stderr)
        return 1
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
