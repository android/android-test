"""Defines current AXT versions and dependencies."""

# AXT versions
# LINT.IfChange
RUNNER_VERSION = "1.6.0-alpha03"  # stable 1.5.1
RULES_VERSION = "1.6.0-alpha01"  # stable 1.5.0
MONITOR_VERSION = "1.7.0-alpha01"  # stable 1.6.0
ESPRESSO_VERSION = "3.6.0-alpha01"  # stable 3.5.0
CORE_VERSION = "1.6.0-alpha01"  # stable 1.5.0
ESPRESSO_DEVICE_VERSION = "1.0.0-alpha05"
ANDROIDX_JUNIT_VERSION = "1.2.0-alpha01"  # stable 1.1.4
ANDROIDX_TRUTH_VERSION = "1.6.0-alpha01"  # stable 1.5.0
ANNOTATION_VERSION = "1.1.0-alpha01"  # stable 1.0.1
ORCHESTRATOR_VERSION = "1.5.0-alpha01"  # stable 1.4.2
# LINT.ThenChange(//depot/google3/third_party/android/androidx_test/gradle-tests/settings.gradle)

# LINT.IfChange(SERVICES_VERSION)
SERVICES_VERSION = "1.5.0-alpha01"  # stable 1.4.2
# LINT.ThenChange(//depot/google3/third_party/android/androidx_test/services/AndroidManifest.xml)

# Full maven artifact strings for apks.
SERVICES_APK_ARTIFACT = "androidx.test.services:test-services:%s" % SERVICES_VERSION
ORCHESTRATOR_ARTIFACT = "androidx.test:orchestrator:%s" % ORCHESTRATOR_VERSION

# Maven dependency versions
ANDROIDX_ANNOTATION_VERSION = "1.2.0"
ANDROIDX_ANNOTATION_EXPERIMENTAL_VERSION = "1.1.0"
ANDROIDX_COMPAT_VERSION = "1.3.1"
ANDROIDX_CONCURRENT_VERSION = "1.1.0"
ANDROIDX_CORE_VERSION = "1.6.0"
ANDROIDX_FRAGMENT_VERSION = "1.3.6"
ANDROIDX_CURSOR_ADAPTER_VERSION = "1.0.0"
ANDROIDX_DRAWER_LAYOUT_VERSION = "1.1.1"
ANDROIDX_LEGACY_SUPPORT_VERSION = "1.0.0"
ANDROIDX_LIFECYCLE_VERSION = "2.3.1"
ANDROIDX_MULTIDEX_VERSION = "2.0.0"
ANDROIDX_RECYCLERVIEW_VERSION = "1.2.1"
ANDROIDX_TRACING_VERSION = "1.1.0"
ANDROIDX_VIEWPAGER_VERSION = "1.0.0"
ANDROIDX_WINDOW_VERSION = "1.0.0"
GOOGLE_MATERIAL_VERSION = "1.4.0"
UIAUTOMATOR_VERSION = "2.2.0"
JANK_VERSION = "1.0.1"

# this should match the kotlin toolchain version eg bazel_rules/rules_kotlin/toolchains/kotlin_jvm/kt_jvm_toolchains.bzl KT_VERSION
# and WORKSPACE:KOTLIN_VERSION
KOTLIN_VERSION = "1.8.20"
GRPC_VERSION = "1.54.1"  # needs to match WORKSPACE:GRPC_VERSION

ATF_VERSION = "3.1.2"  # accessibilitytestframework
JUNIT_VERSION = "4.13.2"
HAMCREST_VERSION = "1.3"
TRUTH_VERSION = "1.1.3"
GUAVA_VERSION = "30.1.1-android"
GUAVA_LISTENABLEFUTURE_VERSION = "1.0"
