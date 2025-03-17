# Description:
#   Exposes test infrastructure as a dagger module.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

licenses(["notice"])

android_library(
    name = "inject",
    testonly = 0,
    srcs = glob(["*java"]),
    visibility = [
        "//:__subpackages__",
    ],
    deps = [
        "//opensource/androidx:annotation",
        "@maven//:javax_inject_javax_inject",
    ],
)
