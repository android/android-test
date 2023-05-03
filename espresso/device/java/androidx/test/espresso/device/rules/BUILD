# Description:
#   Rules that can be used to interact with the device.

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

kt_android_library(
    name = "rules",
    testonly = 1,
    srcs = glob(["*.kt"]),
    deps = [
        "//annotation/java/androidx/test/annotation",
        "//espresso/device/java/androidx/test/espresso/device:device_api",
        "//espresso/device/java/androidx/test/espresso/device/action",
        "//espresso/device/java/androidx/test/espresso/device/controller",
        "//espresso/device/java/androidx/test/espresso/device/util",
        "//runner/monitor",
        "@maven//:junit_junit",
    ],
)
