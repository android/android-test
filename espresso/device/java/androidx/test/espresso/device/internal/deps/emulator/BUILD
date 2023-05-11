# Description:
#  Internal package for jarjar-ed shaded classes

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

android_library(
    name = "emulator",
    testonly = 1,
    srcs = glob(["*.java"]),
    deps = ["@maven//:androidx_annotation_annotation"],
)
