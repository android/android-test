# Description: Build rules for building androidx.test.annotation from source

load("//build_extensions:api_checks.bzl", "api_checks")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

# all users should reference the equivalent targets in //annotation
package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//:__subpackages__"],
)

licenses(["notice"])

android_library(
    name = "annotation",
    srcs = glob(
        ["**/*.java"],
    ),
    manifest = "AndroidManifest.xml",
    tags = ["alt_dep=//annotation"],
    deps = [
        "//opensource/androidx:annotation",
        "@maven//:androidx_annotation_annotation_experimental",
    ],
)

# Generate rules for the release artifacts.
axt_android_aar(
    name = "annotation_aar",
    expected_class_prefixes = [
        "androidx.test.annotation",
    ],
    included_dep = ":annotation",
)

maven_artifact(
    name = "annotation_maven_artifact",
    last_updated = "20210901000000",
    target = ":annotation_aar",
)

dackka_test(
    name = "annotation_doc",
    runtime_dep = ":annotation",
    src_jar = ":annotation_aar-src.jar",
)

api_checks(
    name = "annotation_api",
    runtime_dep = ":annotation",
    src_jar = ":annotation_aar-src.jar",
)
