# Description:
#   Espresso - the new UI Test API for Android.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

REMOTE = [
    "DataInteractionRemote.java",
]

android_library(
    name = "data-interaction-remote",
    srcs = REMOTE,
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/proto:ui_interaction_android_pb3",
        "//espresso/core/java/androidx/test/espresso/proto/matcher:ui_interaction_view_matchers_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
    ],
)
