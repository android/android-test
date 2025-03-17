# This package loads and stores test output artifact metadata for easier debugging of Espresso tests
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
    default_visibility = ["//espresso/core/java/androidx/test/espresso:espresso_pkg"],
)

android_library(
    name = "data",
    testonly = 0,
    srcs = glob([
        "*.java",
    ]),
    deps = [
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/internal/data/model",
        "//opensource/androidx:annotation",
        "//runner/monitor",
    ],
)
