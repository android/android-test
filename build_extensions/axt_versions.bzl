"""Defines next to be released AXT versions.

Use tools/release/validate_and_propagate_versions.sh to propagate these versions to
//:axt_m2_repository and gradle-tests/settings.gradle
"""

RUNNER_VERSION = "1.7.0-alpha02"
RULES_VERSION = "1.7.0-alpha02"
MONITOR_VERSION = "1.8.0-alpha02"
ESPRESSO_VERSION = "3.7.0-alpha02"
CORE_VERSION = "1.7.0-alpha02"
ESPRESSO_DEVICE_VERSION = "1.1.0-alpha02"
ANDROIDX_JUNIT_VERSION = "1.3.0-alpha02"
ANDROIDX_TRUTH_VERSION = "1.7.0-alpha02"
ORCHESTRATOR_VERSION = "1.6.0-alpha03"
SERVICES_VERSION = "1.6.0-alpha03"

# Full maven artifact strings for apks.
SERVICES_APK_ARTIFACT = "androidx.test.services:test-services:%s" % SERVICES_VERSION
ORCHESTRATOR_ARTIFACT = "androidx.test:orchestrator:%s" % ORCHESTRATOR_VERSION
