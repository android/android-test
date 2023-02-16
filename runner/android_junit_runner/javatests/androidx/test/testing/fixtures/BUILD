load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

android_library(
    name = "fixtures",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
    ],
)
