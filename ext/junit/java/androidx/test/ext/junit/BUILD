# Description: Build rules for building androidx.test.junit from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:api_checks.bzl", "api_checks")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:kt_android_library.bzl", "kt_android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")

# all users should reference the equivalent targets in //ext/junit
package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = ["//:__subpackages__"],
)

licenses(["notice"])

kt_android_library(
    name = "junit",
    srcs = glob(
        [
            "**/*.java",
            "**/*.kt",
        ],
    ),
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    tags = ["alt_dep=//ext/junit"],
    deps = [
        "//core",
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "//services/storage",
        "@maven//:junit_junit",
    ],
)

# group of targets to use to produce release binary + docs
android_library(
    name = "junit_release_lib",
    custom_package = "androidx.test.ext.junit",
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    visibility = ["//visibility:private"],
    exports = [":junit"],
    deps = [":junit"],
)

# Generate rules for the release artifacts.
axt_android_aar(
    name = "junit_aar",
    expected_class_prefixes = [
        "androidx.test.ext.junit",
    ],
    included_dep = ":junit_release_lib",
)

maven_artifact(
    name = "junit_maven_artifact",
    last_updated = "20180723000000",
    target = ":junit_aar",
)

dackka_test(
    name = "junit_doc",
    runtime_dep = ":junit",
    src_jar = ":junit_aar-src.jar",
)

api_checks(
    name = "junit_api",
    runtime_dep = ":junit",
    src_jar = ":junit_aar-src.jar",
)
