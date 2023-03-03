load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# Description:
# Espresso Accessibility APIs
licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

android_library(
    name = "accessibility_checks",
    testonly = 1,
    srcs = [
        "AccessibilityChecks.java",
    ],
    manifest = "AndroidManifest.xml",
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:org_hamcrest_hamcrest_core",  # unuseddeps: keep
        "@maven//:org_hamcrest_hamcrest_library",  # unuseddeps: keep
    ],
)

axt_android_aar(
    name = "accessibility_checks_aar",
    testonly = 1,
    expected_class_prefixes = [
        "androidx.test.espresso.accessibility",
    ],
    included_dep = ":accessibility_checks",
)

maven_artifact(
    name = "accessibility_checks_maven_artifact",
    testonly = 1,
    last_updated = "20170622000000",
    target = ":accessibility_checks_aar",
)

dackka_test(
    name = "accessibility_checks_doc",
    runtime_dep = ":accessibility_checks",
    src_jar =
        ":accessibility_checks_aar-src.jar",
)

api_checks(
    name = "accessibility_api",
    runtime_dep = ":accessibility_checks",
    src_jar =
        ":accessibility_checks_aar-src.jar",
)
