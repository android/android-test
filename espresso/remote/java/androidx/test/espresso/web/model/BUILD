# Description:
#   Encoder/Decoders for WebView / JSON.
#   Atom interface for wrapping javascripts.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/remote:__subpackages__"],
)

licenses(["notice"])

android_library(
    name = "remote_web_model_atoms",
    srcs = glob(
        ["*.java"],
    ),
    deps = [
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "//espresso/remote/java/androidx/test/espresso/web/proto/model:remote_web_model_atoms_proto_android_pb3",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)
