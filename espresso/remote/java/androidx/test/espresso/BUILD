# Description:
#   Espresso - the new UI Test API for Android.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/remote:__subpackages__"],
)

android_library(
    name = "data-interaction-remote",
    srcs = [
        "DataInteractionRemote.java",
    ],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/remote/java/androidx/test/espresso/proto:ui_interaction_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:ui_interaction_view_matchers_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "@maven//:androidx_annotation_annotation",
    ],
)
