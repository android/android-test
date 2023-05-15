# Description:
#  Device controller

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

android_library(
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

android_library(
    name = "controller_impl",
    testonly = 1,
    srcs = [
        "PhysicalDeviceController.kt",
    ],
    deps = [
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
    ],
)
