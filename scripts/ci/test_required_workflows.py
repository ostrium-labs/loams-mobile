import unittest
from pathlib import Path

from required_workflows import matching_glob, selected_workflows, runs_complete


class RequiredWorkflowsTests(unittest.TestCase):
    def test_globs_follow_github_path_boundaries(self):
        self.assertTrue(matching_glob("android/**", "android/core/src/Foo.kt"))
        self.assertFalse(matching_glob("android/*", "android/core/src/Foo.kt"))
        self.assertTrue(matching_glob("**/*.md", "docs/setup/readme.md"))

    def test_mobile_paths_select_only_applicable_workflows(self):
        root = Path(__file__).resolve().parents[2]
        active = {str(p.relative_to(root)) for p in (root / ".github/workflows").glob("*.yml")}
        self.assertEqual(
            selected_workflows(root, "pull_request", "dev", ["CONTRIBUTING.md"], active),
            {".github/workflows/dco.yml"},
        )
        selected = selected_workflows(root, "pull_request", "dev", ["android/core/src/Foo.kt"], active)
        self.assertIn(".github/workflows/android.yml", selected)
        self.assertIn(".github/workflows/conformance.yml", selected)
        self.assertNotIn(".github/workflows/ios.yml", selected)

    def test_incomplete_or_failed_expected_run_cannot_pass(self):
        expected = {".github/workflows/android.yml", ".github/workflows/dco.yml"}
        android = {"path": ".github/workflows/android.yml", "status": "completed", "conclusion": "success"}
        dco = {"path": ".github/workflows/dco.yml", "status": "in_progress", "conclusion": None}
        self.assertEqual(runs_complete(expected, [android]), (False, None))
        self.assertEqual(runs_complete(expected, [android, dco]), (False, None))
        dco["status"] = "completed"
        dco["conclusion"] = "failure"
        self.assertEqual(runs_complete(expected, [android, dco]), (False, ".github/workflows/dco.yml: failure"))
        dco["conclusion"] = "success"
        self.assertEqual(runs_complete(expected, [android, dco]), (True, None))


if __name__ == "__main__":
    unittest.main()
