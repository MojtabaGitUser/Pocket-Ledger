import json
import tempfile
import unittest
from pathlib import Path

from scripts.validate_firebase_config import validate_config


class FirebaseConfigValidationTest(unittest.TestCase):
    def write_config(self, clients: list[tuple[str, str]]) -> Path:
        directory = Path(tempfile.mkdtemp())
        path = directory / "google-services.json"
        path.write_text(
            json.dumps(
                {
                    "client": [
                        {
                            "client_info": {
                                "mobilesdk_app_id": app_id,
                                "android_client_info": {"package_name": package},
                            }
                        }
                        for package, app_id in clients
                    ]
                }
            ),
            encoding="utf-8",
        )
        self.addCleanup(lambda: directory.rmdir())
        self.addCleanup(lambda: path.unlink(missing_ok=True))
        return path

    def test_accepts_release_and_debug_clients(self):
        path = self.write_config(
            [
                ("com.mojtaba.folentra", "release-app-id"),
                ("com.mojtaba.folentra.debug", "debug-app-id"),
            ]
        )

        validate_config(
            path,
            {"com.mojtaba.folentra", "com.mojtaba.folentra.debug"},
            "release-app-id",
        )

    def test_rejects_legacy_package(self):
        path = self.write_config(
            [("com.mojtaba.pocketledger", "legacy-app-id")]
        )

        with self.assertRaisesRegex(ValueError, "missing required Android package"):
            validate_config(path, {"com.mojtaba.folentra"}, None)

    def test_rejects_mismatched_distribution_app_id(self):
        path = self.write_config(
            [("com.mojtaba.folentra", "configured-app-id")]
        )

        with self.assertRaisesRegex(ValueError, "does not match"):
            validate_config(
                path,
                {"com.mojtaba.folentra"},
                "different-app-id",
            )


if __name__ == "__main__":
    unittest.main()
