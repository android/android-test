# Alias for androidx.test.espresso.web API library

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_library(
    name = "web",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = ["//espresso/web/java/androidx/test/espresso/web:espresso_web_release_lib"],
)
