# Description:
#   Size classes

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
    name = "sizeclass",
    srcs = glob(["*.kt"]),
    deps = [
        "//runner/monitor",
        "@maven//:androidx_window_window_core",
    ],
)
