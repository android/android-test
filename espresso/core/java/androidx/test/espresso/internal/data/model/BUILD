# This package loads and stores test output artifact metadata for easier debugging of Espresso tests
load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/core/java/androidx/test/espresso:espresso_pkg"],
)

kt_android_library(
    name = "model",
    srcs = glob([
        "*.kt",
        "*.java",
    ]),
    deps = [
        "//espresso/core/java/androidx/test/espresso:framework",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
