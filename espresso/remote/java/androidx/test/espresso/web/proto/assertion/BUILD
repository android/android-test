# Protocol buffer definitions for remote web assertions

load("@rules_java//java:defs.bzl", "java_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/remote:__subpackages__"],
)

proto_library(
    name = "web_assertions_proto",
    srcs = ["web_assertions.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "web_assertions_proto_android_pb3",
    deps = [":web_assertions_proto"],
)

java_library(
    name = "package-info",
    srcs = ["package-info.java"],
    deps = [
        "//opensource/androidx:annotation",
    ],
)
