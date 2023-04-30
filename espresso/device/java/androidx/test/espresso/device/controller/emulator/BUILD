# Description:
#  Emulator controller

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

kt_android_library(
    name = "emulator",
    testonly = 1,
    srcs = glob(["*.kt"]),
    deps = [
        "//espresso/device/java/androidx/test/espresso/device/action",
        "//espresso/device/java/androidx/test/espresso/device/controller",
        "//espresso/device/java/androidx/test/espresso/device/controller/emulator/proto:emulator_controller_java_grpc",
        "//espresso/device/java/androidx/test/espresso/device/controller/emulator/proto:emulator_controller_java_proto_lite",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
        "@maven//:io_grpc_grpc_okhttp",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:org_apache_tomcat_annotations_api",
    ],
)
