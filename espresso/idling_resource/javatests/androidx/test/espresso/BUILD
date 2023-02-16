# Description:
#   Tests for espresso idling resources

load(
    "//build_extensions:android_library_instrumentation_tests.bzl",
    "android_library_instrumentation_tests",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_library_instrumentation_tests(
    name = "instrumentation_tests",
    srcs = glob(["**/*.java"]),
    deps = [
        "//core",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
)
