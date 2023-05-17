# Description:
# JUnit4 Rules for Intents

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

kt_android_library(
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
