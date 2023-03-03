# Protocol buffer definitions for remote view assertions

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/remote:__subpackages__"],
)

proto_library(
    name = "view_assertions_proto",
    srcs = ["view_assertions.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "ui_interaction_view_assertions_android_pb3",
    deps = [":view_assertions_proto"],
)

java_library(
    name = "package-info",
    srcs = ["package-info.java"],
    deps = [
        "@maven//:androidx_annotation_annotation",
    ],
)
