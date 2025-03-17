load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
)

licenses(["notice"])

android_library(
    name = "protocol",
    srcs = ["SpeakEasyProtocol.java"],
    visibility = [
        "//services:__subpackages__",
    ],
)
