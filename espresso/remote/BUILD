# Alias for androidx.test.espresso.remote API library

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

licenses(["notice"])

android_library(
    name = "remote",
    visibility = ["//visibility:public"],
    exports = [
        "//espresso/remote/java/androidx/test/espresso/remote",
        "//espresso/remote/java/androidx/test/espresso/web/proto/assertion:web_assertions_proto_android_pb3",
    ],
)
