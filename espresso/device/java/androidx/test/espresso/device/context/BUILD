# Description:
#  Provides context for device operations.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

android_library(
    name = "context",
    srcs = ["ActionContext.kt"],
    deps = [
        "@maven//:androidx_annotation_annotation",
    ],
)

android_library(
    name = "context_impl",
    srcs = ["InstrumentationTestActionContext.kt"],
    deps = [
        ":context",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
    ],
)
