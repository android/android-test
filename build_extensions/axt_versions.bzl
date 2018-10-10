"""Defines current AXT versions and dependencies.

Ensure UsageTrackerRegistry is updated accordingly when incrementing version numbers.
"""

# AXT versions
RUNNER_VERSION = "1.1.0-beta02"
ESPRESSO_VERSION = "3.1.0-beta02"
CORE_VERSION = "1.0.0-beta02"
ANDROIDX_JUNIT_VERSION = CORE_VERSION
ANDROIDX_TRUTH_VERSION = CORE_VERSION
UIAUTOMATOR_VERSION = "2.2.0-beta02"
JANK_VERSION = "1.0.1-beta02"

# Maven dependency versions
ANDROIDX_VERSION = "1.0.0"
ANDROIDX_VERSION_PATH = "1.0.0"
GOOGLE_MATERIAL_VERSION = "1.0.0"
ANDROIDX_ARCH_COMPONENTS_VERSION = "2.0.0"

# TODO(b/114419674): Currently bazel can not find newer versions. Ideally androidx would be used
# instead
ANDROID_SUPPORT_LIBRARY_VERSION = "27.0.2"

JUNIT_VERSION = "4.12"
HAMCREST_VERSION = "1.3"
TRUTH_VERSION = "0.42"
GUAVA_VERSION = "26.0-android"
