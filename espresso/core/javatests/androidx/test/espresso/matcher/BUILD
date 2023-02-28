# Description:
#   Tests for espresso matchers

load(
    "//build_extensions:android_app_instrumentation_tests.bzl",
    "android_app_instrumentation_tests",
)
load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
    "gmscore_channel",
)
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_library(
    name = "utils",
    srcs = ["MatcherTestUtils.java"],
    visibility = ["//visibility:private"],
    deps = ["@maven//:org_hamcrest_hamcrest_core"],
)

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    size = "large",
    srcs = glob(
        ["*Test.java"],
        exclude = ["ViewMatchers2Test.java"],
    ),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "@androidsdk//:legacy_test-30",
        "@maven//:androidx_annotation_annotation",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

# TODO(b/232962936): AOSP API 26 image does not include webview
android_app_instrumentation_tests(
    name = "webview_tests",
    size = "large",
    srcs = ["ViewMatchers2Test.java"],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    device_list = devices(apis(exclude_apis = [26])) + devices(
        [26],
        gms_channel = gmscore_channel.PREBUILT,
    ),
    shard_count = 4,
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "@androidsdk//:legacy_test-30",
        "@maven//:androidx_annotation_annotation",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)
