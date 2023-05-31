# Description:
#  Espresso Device shared utils

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

kt_android_library(
    name = "common",
    testonly = 1,
    srcs = glob([
        "*.kt",
        "*.java",
    ]),
    deps = [
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
    ],
)
