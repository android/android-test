# Description:
#   Tests for espresso util classes

load(
    "//build_extensions:android_app_instrumentation_tests.bzl",
    "android_app_instrumentation_tests",
)
load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    srcs = glob(["*.java"]),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    deps = [
        "//espresso/core/java/androidx/test/espresso/util",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

axt_android_local_test(
    name = "HumanReadablesTest",
    srcs = ["HumanReadablesTest.java"],
    deps = [
        "//espresso/core/java/androidx/test/espresso/util",
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
    ],
)
