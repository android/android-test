# Description:
#  Matchers for Intents and Intent-related objects

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "matcher",
    testonly = 1,
    srcs = glob(["*.java"]),
    deps = [
        "//core",
        "//espresso/intents/java/androidx/test/espresso/intent:precondition_checks",
        "//espresso/intents/java/androidx/test/espresso/intent:resolved_intent_interface",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
