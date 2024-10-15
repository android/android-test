"""Defines next to be released AXT versions.

Use tools/release/validate_and_propagate_versions.sh to propagate these versions to
//:axt_m2_repository and gradle-tests/settings.gradle
"""

RUNNER_VERSION = "1.6.2"
RULES_VERSION = "1.6.1"
MONITOR_VERSION = "1.7.2"
ESPRESSO_VERSION = "3.6.1"
CORE_VERSION = "1.6.1"
ESPRESSO_DEVICE_VERSION = "1.0.1"
ANDROIDX_JUNIT_VERSION = "1.2.1"
ANDROIDX_TRUTH_VERSION = "1.6.0"
ORCHESTRATOR_VERSION = "1.5.1"
SERVICES_VERSION = "1.5.0"

# Full maven artifact strings for apks.
SERVICES_APK_ARTIFACT = "androidx.test.services:test-services:%s" % SERVICES_VERSION
ORCHESTRATOR_ARTIFACT = "androidx.test:orchestrator:%s" % ORCHESTRATOR_VERSION
