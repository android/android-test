# Description:
#  Matchers for Espresso!

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/remote:__subpackages__"],
)

REMOTE_HAMCREST_v1_3_CORE_MATCHERS = [
    "RemoteHamcrestCoreMatchers13.java",
]

android_library(
    name = "remote_view_matchers",
    srcs = glob(
        ["*.java"],
        exclude = REMOTE_HAMCREST_v1_3_CORE_MATCHERS,
    ),
    deps = [
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:hamcrest_matchers_v1_3_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:ui_interaction_root_matchers_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:ui_interaction_view_matchers_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "@maven//:androidx_annotation_annotation",
    ],
)

android_library(
    name = "remote_hamcrest_v1_3_core_matchers",
    srcs = glob(
        REMOTE_HAMCREST_v1_3_CORE_MATCHERS,
    ),
    deps = [
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:hamcrest_matchers_v1_3_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
