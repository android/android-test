# Publicly visible junit extensions for Android

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

android_library(
    name = "junit",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = ["//ext/junit/java/androidx/test/ext/junit"],
)
