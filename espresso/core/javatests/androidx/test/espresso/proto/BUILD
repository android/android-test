# Protocol buffer definitions for any proto transformers. Only used in tests!

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//opensource/visibility:testing_pkg"],
)

proto_library(
    name = "test_protos",
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
