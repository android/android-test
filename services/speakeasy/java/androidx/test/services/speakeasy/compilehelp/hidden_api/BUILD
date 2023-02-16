load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//services:license"])

licenses(["notice"])

android_library(
    name = "IActivityManager",
    srcs = ["IActivityManager.java"],
    neverlink = 1,
    deps = [
        ":IApplicationThread",
    ],
)

android_library(
    name = "ActivityManagerNative",
    srcs = ["ActivityManagerNative.java"],
    neverlink = 1,
    visibility = [
        "//services/speakeasy/java/androidx/test/services/speakeasy/client:__pkg__",
    ],
    deps = [":IActivityManager"],
)

android_library(
    name = "IApplicationThread",
    srcs = ["IApplicationThread.java"],
)
