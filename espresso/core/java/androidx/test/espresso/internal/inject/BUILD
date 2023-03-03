# Description:
#   Exposes test infrastructure as a dagger module.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_library(
    name = "inject",
    srcs = glob(["*java"]),
    visibility = [
        ":__pkg__",
        "//:__subpackages__",
    ],
    deps = [
        "@maven//:androidx_annotation_annotation",
        "@maven//:javax_inject_javax_inject",
    ],
)
