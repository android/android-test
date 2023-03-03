# This package loads and stores test output artifact metadata for easier debugging of Espresso tests
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/core/java/androidx/test/espresso:espresso_pkg"],
)

android_library(
    name = "data",
    srcs = glob([
        "*.java",
    ]),
    deps = [
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/internal/data/model",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
    ],
)
