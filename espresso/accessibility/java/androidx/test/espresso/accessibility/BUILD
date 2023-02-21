load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ATF_VERSION", "ESPRESSO_VERSION")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:combine_jars.bzl", "combine_jars")
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
        "//opensource:hamcrest",  # unuseddeps: keep
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
    ],
)

axt_release_lib(
    name = "accessibility_checks_release",
    keep_spec = "androidx/test/espresso/accessibility",
    multidex = "native",
    remove_spec = "androidx/test/espresso/accessibility/R[$$\\.]",
    deps = [
        ":accessibility_checks",
    ],
)

filegroup(
    name = "accessibility_checks_src",
    srcs = [
        ":libaccessibility_checks-src.jar",
    ],
)

combine_jars(
    name = "accessibility_checks_src_jars",
    srcs = [
        ":accessibility_checks_src",
    ],
)

dackka_test(
    name = "accessibility_checks_doc",
    runtime_dep = ":accessibility_checks",
    src_jar =
        ":accessibility_checks_src_jars.jar",
)

maven_artifact(
    name = "accessibility_checks_maven_artifact",
    src = ":accessibility_checks_release.aar",
    artifact_deps = [
        "androidx.test.espresso:espresso-core:%s" % ESPRESSO_VERSION,
        "com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework:%s" % ATF_VERSION,
    ],
    artifact_id = "espresso-accessibility",
    group_id = "androidx.test.espresso",
    last_updated = "20170622000000",
    src_jar = ":accessibility_checks_src_jars.jar",
    version = "%s" % ESPRESSO_VERSION,
)

api_checks(
    name = "accessibility_api",
    runtime_dep = ":accessibility_checks",
    src_jar = ":accessibility_checks_src_jars.jar",
)
