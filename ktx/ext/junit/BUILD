# Publicly visible androidx.ext.junit Kotlin extension library

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

android_library(
    name = "junit",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = ["//ktx/ext/junit/java/androidx/test/ext/junit"],
)
