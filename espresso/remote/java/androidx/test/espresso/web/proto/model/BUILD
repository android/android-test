# Protocol buffer definitions for remote web model atoms

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/remote:__subpackages__"],
)

proto_library(
    name = "remote_web_model_atoms_proto",
    srcs = ["web_model_atoms.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "remote_web_model_atoms_proto_android_pb3",
    deps = [":remote_web_model_atoms_proto"],
)

java_library(
    name = "package-info",
    srcs = ["package-info.java"],
    deps = [
        "@maven//:androidx_annotation_annotation",
    ],
)
