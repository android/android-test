load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# Description: Build rules for building androidx.test from source
licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = [
        "//:__pkg__",
        "//runner/rules:__pkg__",
    ],
)

# test rules
android_library(
    name = "rules",
    srcs = glob(["**/*.java"]),
    custom_package = "androidx.test.rules",
    manifest = "AndroidManifest.xml",
    proguard_specs = [
        "proguard.cfg",
    ],
    deps = [
        "//annotation",
        "//runner/android_junit_runner",
        "@androidsdk//:legacy_test-30",
        "@maven//:androidx_annotation_annotation",
        "@maven//:junit_junit",
    ],
)

axt_android_aar(
    name = "rules_aar",
    expected_class_prefixes = [
        "androidx.test.annotation.UiThreadTest",
        "androidx.test.internal.statement",
        "androidx.test.rule",
    ],
    included_dep = ":rules",
)

maven_artifact(
    name = "rules_maven_artifact",
    last_updated = "20170622000000",
    target = ":rules_aar",
)

dackka_test(
    name = "rules_doc",
    runtime_dep = ":rules",
    src_jar = ":rules_aar-src.jar",
)

api_checks(
    name = "rules_api",
    runtime_dep = ":rules",
    src_jar = ":rules_aar-src.jar",
)
