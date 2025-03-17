# Description:
#  Device Actions for espresso.

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
    name = "action",
    srcs = glob(["*.kt"]),
    deps = [
        "//espresso/device/java/androidx/test/espresso/device/common",
        "//espresso/device/java/androidx/test/espresso/device/controller",
        "//espresso/device/java/androidx/test/espresso/device/sizeclass",
        "//runner/monitor",
        "@maven//:androidx_window_window",
        "@maven//:androidx_window_window_java",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_android",
    ],
)
