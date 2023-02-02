"""Defines current AXT versions and dependencies."""

# AXT versions
RUNNER_VERSION = "1.6.0-alpha02"  # stable 1.5.1
RULES_VERSION = "1.6.0-alpha02"  # stable 1.5.0
MONITOR_VERSION = "1.7.0-alpha02"  # stable 1.6.0
ESPRESSO_VERSION = "3.6.0-alpha02"  # stable 3.5.0
CORE_VERSION = "1.6.0-alpha02"  # stable 1.5.0
ESPRESSO_DEVICE_VERSION = "1.0.0-alpha05"
ANDROIDX_JUNIT_VERSION = "1.2.0-alpha02"  # stable 1.1.4
ANDROIDX_TRUTH_VERSION = "1.6.0-alpha02"  # stable 1.5.0

# LINT.IfChange(SERVICES_VERSION)
SERVICES_VERSION = "1.5.0-alpha02"  # stable 1.4.2

# LINT.ThenChange(//depot/google3/third_party/android/androidx_test/services/AndroidManifest.xml)
ORCHESTRATOR_VERSION = "1.5.0-alpha02"  # stable 1.4.2
ANNOTATION_VERSION = "1.1.0-alpha02"  # stable 1.0.1

# Full maven artifact strings. These cannot change for already published artifacts
RUNNER_ARTIFACT = "androidx.test:runner:%s" % RUNNER_VERSION
RULES_ARTIFACT = "androidx.test:rules:%s" % RULES_VERSION
MONITOR_ARTIFACT = "androidx.test:monitor:%s" % MONITOR_VERSION
CORE_ARTIFACT = "androidx.test:core:%s" % CORE_VERSION
CORE_KTX_ARTIFACT = "androidx.test:core-ktx:%s" % CORE_VERSION
ESPRESSO_ACCESSIBILITY_ARTIFACT = "androidx.test.espresso:espresso-accessibility:%s" % ESPRESSO_VERSION
ESPRESSO_CONTRIB_ARTIFACT = "androidx.test.espresso:espresso-contrib:%s" % ESPRESSO_VERSION
ESPRESSO_CORE_ARTIFACT = "androidx.test.espresso:espresso-core:%s" % ESPRESSO_VERSION
ESPRESSO_DEVICE_ARTIFACT = "androidx.test.espresso:espresso-device:%s" % ESPRESSO_DEVICE_VERSION
ESPRESSO_IDLING_ARTIFACT = "androidx.test.espresso:espresso-idling-resource:%s" % ESPRESSO_VERSION
ESPRESSO_IDLING_CONCURRENT_ARTIFACT = "androidx.test.espresso.idling:idling-concurrent:%s" % ESPRESSO_VERSION
ESPRESSO_IDLING_NET_ARTIFACT = "androidx.test.espresso.idling:idling-net:%s" % ESPRESSO_VERSION
ESPRESSO_INTENTS_ARTIFACT = "androidx.test.espresso:espresso-intents:%s" % ESPRESSO_VERSION
ESPRESSO_REMOTE_ARTIFACT = "androidx.test.espresso:espresso-remote:%s" % ESPRESSO_VERSION
ESPRESSO_WEB_ARTIFACT = "androidx.test.espresso:espresso-web:%s" % ESPRESSO_VERSION
ANDROIDX_JUNIT_ARTIFACT = "androidx.test.ext:junit:%s" % ANDROIDX_JUNIT_VERSION
ANDROIDX_JUNIT_KTX_ARTIFACT = "androidx.test.ext:junit-ktx:%s" % ANDROIDX_JUNIT_VERSION
ANDROIDX_TRUTH_ARTIFACT = "androidx.test.ext:truth:%s" % ANDROIDX_TRUTH_VERSION
SERVICES_STORAGE_ARTIFACT = "androidx.test.services:storage:%s" % SERVICES_VERSION
SERVICES_APK_ARTIFACT = "androidx.test.services:test-services:%s" % SERVICES_VERSION
ORCHESTRATOR_ARTIFACT = "androidx.test:orchestrator:%s" % ORCHESTRATOR_VERSION
ANNOTATION_ARTIFACT = "androidx.test:annotation:%s" % ANNOTATION_VERSION

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
GOOGLE_MATERIAL_VERSION = "1.4.0"
UIAUTOMATOR_VERSION = "2.2.0"
JANK_VERSION = "1.0.1"

# this should match the kotlin toolchain version eg bazel_rules/rules_kotlin/toolchains/kotlin_jvm/kt_jvm_toolchains.bzl KT_VERSION
KOTLIN_VERSION = "1.7.10"

# accessibilitytestframework
ATF_VERSION = "3.1.2"
JUNIT_VERSION = "4.13.2"
HAMCREST_VERSION = "1.3"
TRUTH_VERSION = "1.1.3"
GUAVA_VERSION = "30.1.1-android"
GUAVA_LISTENABLEFUTURE_VERSION = "1.0"
