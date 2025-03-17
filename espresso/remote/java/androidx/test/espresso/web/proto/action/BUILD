# Protocol buffer definitions for remote web actions

load("@com_google_protobuf//bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("@com_google_protobuf//bazel:proto_library.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
    default_visibility = ["//espresso/remote:__subpackages__"],
)

proto_library(
    name = "web_actions_proto",
    testonly = 0,
    srcs = ["web_actions.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "web_actions_proto_android_pb3",
    deps = [":web_actions_proto"],
)

java_library(
    name = "package-info",
    testonly = 0,
    srcs = ["package-info.java"],
    deps = [
        "//opensource/androidx:annotation",
    ],
)
