# Protocol buffer definitions for remote UI interactions

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
    name = "ui_interaction_proto",
    srcs = ["ui_interaction.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "ui_interaction_android_pb3",
    deps = [
        ":ui_interaction_proto",
    ],
)

# gather up the package-infos which hide the proto generated code from public API
java_library(
    name = "package-infos",
    srcs = ["package-info.java"],
    exports = [
        "//espresso/remote/java/androidx/test/espresso/proto/action:package-info",
        "//espresso/remote/java/androidx/test/espresso/proto/assertion:package-info",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:package-info",
    ],
    deps = [
        "//opensource/androidx:annotation",
    ],
)
