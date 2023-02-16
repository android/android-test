# Alias for androidx.test.espresso.core API library

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_library(
    name = "core",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = ["//espresso/core/java/androidx/test/espresso"],
)
