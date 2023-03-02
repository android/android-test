# Description:
#   Common library for testing inter and intra app communication via intents.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

android_library(
    name = "intents",
    testonly = 1,
    srcs = glob(
        ["*.java"],
        exclude = [
            "Checks.java",
            "ResolvedIntent.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        ":precondition_checks",
        ":resolved_intent_interface",
        "//annotation",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//runner/android_junit_runner",
        "@androidsdk//:legacy_test-30",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_library(
    name = "precondition_checks",
    srcs = [
        "Checks.java",
    ],
)

android_library(
    name = "resolved_intent_interface",
    srcs = ["ResolvedIntent.java"],
)

# ** Generate the release artifacts **

android_library(
    name = "espresso_intents_aar_lib",
    testonly = 1,
    manifest = "AndroidManifest.xml",
    exports = [
        ":intents",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
    ],
)

# Generates the release artifacts
axt_android_aar(
    name = "espresso_intents_aar",
    testonly = 1,
    expected_class_prefixes = [
        "androidx.test.espresso.intent",
    ],
    included_dep = ":espresso_intents_aar_lib",
)

maven_artifact(
    name = "espresso_intents_maven_artifact",
    testonly = 1,
    last_updated = "20170622000000",
    target = ":espresso_intents_aar",
)

dackka_test(
    name = "espresso_intents_doc",
    runtime_dep = ":espresso_intents_aar_lib",
    src_jar = ":espresso_intents_aar-src.jar",
)

api_checks(
    name = "espresso_intents_api",
    runtime_dep = ":espresso_intents_aar_lib",
    src_jar = ":espresso_intents_aar-src.jar",
)
