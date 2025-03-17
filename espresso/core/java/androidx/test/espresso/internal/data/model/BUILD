# This package loads and stores test output artifact metadata for easier debugging of Espresso tests
load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
    default_visibility = ["//espresso/core/java/androidx/test/espresso:espresso_pkg"],
)

kt_android_library(
    name = "model",
    testonly = 0,
    srcs = glob([
        "*.kt",
        "*.java",
    ]),
    deps = [
        "//espresso/core/java/androidx/test/espresso:framework",
        "//opensource/androidx:annotation",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
