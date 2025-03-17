load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
    default_visibility = [
        "//:__subpackages__",
    ],
)

android_library(
    name = "events",
    testonly = 0,
    srcs = glob(["**/*.java"]),
    idl_parcelables = [
        "discovery/TestDiscoveryEvent.aidl",
        "platform/TestPlatformEvent.aidl",
        "run/TestRunEvent.aidl",
    ],
    idl_srcs = [
        "discovery/ITestDiscoveryEvent.aidl",
        "platform/ITestPlatformEvent.aidl",
        "run/ITestRunEvent.aidl",
    ],
    deps = [
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:junit_junit",
    ],
)
