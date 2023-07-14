# Description:
#  Utility classes

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//espresso:__subpackages__"],
)

android_library(
    name = "concurrent",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    deps = [
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation_jvm",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)
