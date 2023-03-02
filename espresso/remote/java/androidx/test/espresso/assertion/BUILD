# Description:
#  View Assertions for New UI Test API.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/remote:__subpackages__"],
)

android_library(
    name = "remote_view_assertions",
    srcs = glob(
        ["*.java"],
    ),
    deps = [
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/remote/java/androidx/test/espresso/proto/assertion:ui_interaction_view_assertions_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "@maven//:androidx_annotation_annotation",
    ],
)
