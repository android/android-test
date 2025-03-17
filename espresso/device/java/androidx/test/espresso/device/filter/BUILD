# Description:
#   Filters that can be used to skip tests based on device properties.

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

kt_android_library(
    name = "filter",
    srcs = glob(["*.kt"]),
    deps = [
        "//espresso/device/java/androidx/test/espresso/device/common",
        "//espresso/device/java/androidx/test/espresso/device/controller",
        "//espresso/device/java/androidx/test/espresso/device/sizeclass",
        "//runner/android_junit_runner",
        "//runner/monitor",
        "@maven//:androidx_window_window_core",
        "@maven//:junit_junit",
    ],
)

kt_android_library(
    name = "requires_display_filter",
    srcs = [
        "RequiresDisplay.kt",
        "RequiresDisplayFilter.kt",
    ],
    deps = [
        "//espresso/device/java/androidx/test/espresso/device/sizeclass",
        "//runner/android_junit_runner",
        "//runner/monitor",
        "@maven//:androidx_window_window_core",
        "@maven//:junit_junit",
    ],
)
