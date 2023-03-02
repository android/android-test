# Description: Build rules for building androidx.test.junit from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# all users should reference the equivalent targets in //ext/junit
package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//:__subpackages__"],
)

licenses(["notice"])

android_library(
    name = "junit",
    testonly = 1,
    srcs = glob(
        ["**/*.java"],
    ),
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    tags = ["alt_dep=//ext/junit"],
    deps = [
        "//core",
        "//runner/monitor",
        "//services/storage",
        "@maven//:androidx_annotation_annotation",
        "@maven//:junit_junit",
    ],
)

# Generate rules for the release artifacts.
axt_android_aar(
    name = "junit_aar",
    testonly = 1,
    expected_class_prefixes = [
        "androidx.test.ext.junit",
    ],
    included_dep = ":junit",
)

maven_artifact(
    name = "junit_maven_artifact",
    testonly = 1,
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
