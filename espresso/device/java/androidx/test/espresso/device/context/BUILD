# Description:
#  Provides context for device operations.

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

kt_android_library(
    name = "context",
    srcs = ["ActionContext.kt"],
    deps = [
        "@maven//:androidx_annotation_annotation",
    ],
)

kt_android_library(
    name = "context_impl",
    srcs = ["InstrumentationTestActionContext.kt"],
    deps = [
        ":context",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
    ],
)
