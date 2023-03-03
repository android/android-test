# Description:
#   Tests for espresso core

load(
    "//build_extensions:android_app_instrumentation_tests.bzl",
    "android_app_instrumentation_tests",
)
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_library(
    name = "utils",
    srcs = [
        "StringPatternMatcher.java",
        "TestTracer.java",
    ],
    visibility = [
        "//espresso/core/javatests/androidx/test/espresso:__subpackages__",
    ],
    deps = [
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    srcs = glob(
        ["*.java"],
        exclude = [
            "StringPatternMatcher.java",
            "TestTracer.java",
        ],
    ),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/internal/data",
        "//espresso/core/java/androidx/test/espresso/remote:bindable",
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/util/concurrent",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_concurrent_concurrent_futures",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)
