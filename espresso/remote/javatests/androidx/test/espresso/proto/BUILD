# Protocol buffer definitions for any proto transformers. Only used in tests!

load("@com_google_protobuf//bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("@com_google_protobuf//bazel:proto_library.bzl", "proto_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
    default_visibility = ["//opensource/visibility:testing_pkg"],
)

proto_library(
    name = "test_protos",
    testonly = 0,
    srcs = ["test_protos.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "test_protos_android_pb3",
    deps = [
        ":test_protos",
    ],
)
