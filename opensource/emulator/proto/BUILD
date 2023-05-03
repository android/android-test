# Protos for interacting with emulator

load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

proto_library(
    name = "emulator_controller_proto",
    srcs = ["emulator_controller.proto"],
    deps = [
        "@com_google_protobuf//:empty_proto",
    ],
)

java_lite_proto_library(
    name = "emulator_controller_java_proto_lite",
    deps = [":emulator_controller_proto"],
)

java_grpc_library(
    name = "emulator_controller_java_grpc",
    srcs = [":emulator_controller_proto"],
    flavor = "lite",
    deps = [":emulator_controller_java_proto_lite"],
)
