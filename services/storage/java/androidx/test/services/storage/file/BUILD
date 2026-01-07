load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
    default_visibility = [
        "//services/storage:__subpackages__",
    ],
)

licenses(["notice"])

# Intended for internal use only.
android_library(
    name = "file",
    srcs = glob(["*.java"]),
    deps = [
        "//runner/monitor",
        "//services/storage/java/androidx/test/services/storage:test_storage_constants",
    ],
)
