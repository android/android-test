# Description: Build rules for building androidx.test.truth from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# all users should reference the equivalent targets in //ext/truth
package(
    default_applicable_licenses = ["//:license"],
    default_visibility = [
        "//:__pkg__",
        "//ext/truth:__subpackages__",
    ],
)

licenses(["notice"])

android_library(
    name = "truth",
    testonly = 1,
    srcs = glob(
        ["**/*.java"],
    ),
    manifest = "AndroidManifest.xml",
    deps = [
        "//annotation",
        "//core",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
    ],
)

# Generate rules for the release artifacts.
axt_android_aar(
    name = "truth_aar",
    testonly = 1,
    expected_class_prefixes = [
        "androidx.test.ext.truth",
    ],
    included_dep = ":truth",
)

maven_artifact(
    name = "truth_maven_artifact",
    testonly = 1,
    last_updated = "20180321000000",
    target = ":truth_aar",
)

dackka_test(
    name = "truth_doc",
    runtime_dep = ":truth",
    src_jar = ":truth_aar-src.jar",
)

api_checks(
    name = "truth_api",
    runtime_dep = ":truth",
    src_jar = ":truth_aar-src.jar",
)
