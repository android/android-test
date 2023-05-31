# Description:
#  Device controller

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

kt_android_library(
    name = "controller",
    srcs = [
        "DeviceControllerOperationException.kt",
        "DeviceMode.kt",
    ],
    deps = [
        "//annotation/java/androidx/test/annotation",
        "//espresso/core/java/androidx/test/espresso:interface",
        "@maven//:androidx_annotation_annotation",
    ],
)

kt_android_library(
    name = "controller_impl",
    testonly = 1,
    srcs = [
        "PhysicalDeviceController.kt",
    ],
    deps = [
        "//espresso/device/java/androidx/test/espresso/device/common",
        "//espresso/device/java/androidx/test/espresso/device/controller",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
    ],
)
