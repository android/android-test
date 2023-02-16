# Publicly visible alias to the runner target

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

# This needs to be a android_library so manifests are exported
android_library(
    name = "android_junit_runner",
    visibility = ["//visibility:public"],
    exports = ["//runner/android_junit_runner/java/androidx/test:runner"],
)
