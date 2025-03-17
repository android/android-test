# Description: Build rules for building androidx.test.truth from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:api_checks.bzl", "api_checks")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:kt_android_library.bzl", "kt_android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")

# all users should reference the equivalent targets in //core
package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = [
        ":allowlist",
    ],
)

package_group(
    name = "allowlist",
    packages = [
        "//...",
    ],
)

licenses(["notice"])

# target containing bootstrap activity manifest entries
# Add this to your application under test to avoid a separate test process getting launched
# for the bootstrap activities
android_library(
    name = "manifest",
    testonly = 0,
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
)

kt_android_library(
    name = "core_internal",
    testonly = 0,
    srcs = glob(
        [
            "**/*.java",
            "**/*.kt",
        ],
    ),
    tags = ["alt_dep=//core"],
    visibility = ["//visibility:private"],
    deps = [
        ":manifest",
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:androidx_concurrent_concurrent_futures_ktx",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:androidx_tracing_tracing",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)

# TODO(b/199522848): delete these targets once espresso moves to testonly
android_library(
    name = "core",
    exports = [":core_internal"],
)

android_library(
    name = "core_not_test_only",
    testonly = 0,
    visibility = ["//espresso/core/java/androidx/test/espresso:__subpackages__"],
    exports = [":core_internal"],
)

# kt_android_library does not produce an aar, so wrap in a android_library for release
android_library(
    name = "core_aar_lib",
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    visibility = ["//visibility:private"],
    exports = [
        ":core_internal",
    ],
)

alias(
    name = "core-src",
    actual = ":core_aar-src.jar",
    visibility = ["//visibility:private"],
)

# Generate rules for the release artifacts.
axt_android_aar(
    name = "core_aar",
    expected_class_prefixes = [
        "androidx.test.core",
    ],
    included_dep = ":core_aar_lib",
)

maven_artifact(
    name = "core_maven_artifact",
    last_updated = "20180403000000",
    target = ":core_aar",
)

dackka_test(
    name = "core_doc",
    runtime_dep = ":core_aar_lib",
    src_jar = ":core_aar-src.jar",
)

api_checks(
    name = "core_api",
    runtime_dep = ":core_aar_lib",
    src_jar = ":core_aar-src.jar",
)
