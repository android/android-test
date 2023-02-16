# Publicly visible androidx.test.core API library

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

android_library(
    name = "core",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = ["//core/java/androidx/test/core"],
)

# The manifest entries for ActivityScenario's bootstrap activities.
# Add this to to your application under test to avoid a separate test process getting launched
# for the bootstrap activities
android_library(
    name = "manifest",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = ["//core/java/androidx/test/core:manifest"],
)
