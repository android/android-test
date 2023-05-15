# Description:
# JUnit4 Rules for Intents

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "rule",
    testonly = 1,
    srcs = glob(
        [
            "*.java",
            "*.kt",
        ],
    ),
    deps = [
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//runner/rules",
        "@maven//:junit_junit",
    ],
)
