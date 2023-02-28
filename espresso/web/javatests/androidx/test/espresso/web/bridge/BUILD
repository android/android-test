# Description: Tests for espresso web

load(
    "//build_extensions:android_app_instrumentation_tests.bzl",
    "android_app_instrumentation_tests",
)
load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    testonly = 1,
    srcs = glob(
        ["**/*.java"],
    ),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    # bridge is only needed for APIs <=18
    device_list = devices(apis(max_api = 18)),
    deps = [
        "//espresso/core",
        "//espresso/web/java/androidx/test/espresso/web/bridge",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "@maven//:androidx_concurrent_concurrent_futures",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
