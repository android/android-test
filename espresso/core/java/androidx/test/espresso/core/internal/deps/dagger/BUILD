# Description:
#   Internal package that will contain shaded dagger classes for external release

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = ["//espresso/core/java/androidx/test/espresso:__pkg__"],
)

android_library(
    name = "dagger",
    srcs = glob(["*java"]),
    deps = [
        "//opensource/androidx:annotation",
    ],
)
