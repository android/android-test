# Description:
#  Emulator controller

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
    name = "emulator",
    srcs = glob(["*.kt"]),
    deps = [
        "//espresso/device/java/androidx/test/espresso/device/action",
        "//espresso/device/java/androidx/test/espresso/device/common",
        "//espresso/device/java/androidx/test/espresso/device/controller",
        "//opensource/androidx:annotation",
        "//opensource/emulator/proto:emulator_controller_java_grpc",
        "//opensource/emulator/proto:emulator_controller_java_proto_lite",
        "//runner/monitor",
        "@@grpc-java~//okhttp:okhttp",
        "@@grpc-java~//core",
        "@@grpc-java~//stub",
    ],
)
