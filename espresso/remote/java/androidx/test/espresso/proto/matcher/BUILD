# Protocol buffer definitions for remote root, view and hamcrest matchers (v1.3)

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
    name = "root_matchers_proto",
    testonly = 0,
    srcs = ["root_matchers.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "ui_interaction_root_matchers_android_pb3",
    deps = [":root_matchers_proto"],
)

proto_library(
    name = "view_matchers_proto",
    testonly = 0,
    srcs = ["view_matchers.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "ui_interaction_view_matchers_android_pb3",
    deps = [":view_matchers_proto"],
)

proto_library(
    name = "hamcrest_matchersv1_3_proto",
    testonly = 0,
    srcs = ["hamcrest_matchersv1_3.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "hamcrest_matchers_v1_3_android_pb3",
    deps = [":hamcrest_matchersv1_3_proto"],
)

java_library(
    name = "package-info",
    testonly = 0,
    srcs = ["package-info.java"],
    deps = [
        "//opensource/androidx:annotation",
    ],
)
