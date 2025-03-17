load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
)

licenses(["notice"])

android_library(
    name = "IActivityManager",
    testonly = 0,
    srcs = ["IActivityManager.java"],
    neverlink = 1,
    deps = [
        ":IApplicationThread",
    ],
)

android_library(
    name = "ActivityManagerNative",
    testonly = 0,
    srcs = ["ActivityManagerNative.java"],
    neverlink = 1,
    visibility = [
        "//services/speakeasy/java/androidx/test/services/speakeasy/client:__pkg__",
    ],
    deps = [":IActivityManager"],
)

android_library(
    name = "IApplicationThread",
    testonly = 0,
    srcs = ["IApplicationThread.java"],
)
