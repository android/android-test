# Description:
#   Size classes
#   TODO(b/236387720): Replace these classes with core window module size classes when available

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

kt_android_library(
    name = "sizeclass",
    testonly = 1,
    srcs = glob(["*.kt"]),
    deps = [
        "//annotation",
    ],
)
