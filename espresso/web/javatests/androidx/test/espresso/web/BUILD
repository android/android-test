# Description: Tests for espresso web

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

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    testonly = 1,
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "action/JavaScriptExecutionActionTest.java",
            "assertion/WebViewAssertionsTest.java",
        ],
    ),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    deps = [
        "//core",
        "//espresso/core",
        "//espresso/web",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "@androidsdk//:legacy_test-30",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

android_app_instrumentation_tests(
    name = "webview_tests",
    srcs = [
        "action/JavaScriptExecutionActionTest.java",
        "assertion/WebViewAssertionsTest.java",
    ],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",

    # TODO(b/232962936): AOSP API 26 doesn't include webview
    device_list = devices(apis(exclude_apis = [26])) + devices(
        [26],
        gms_channel = gmscore_channel.PREBUILT,
    ),
    deps = [
        "//core",
        "//espresso/core",
        "//espresso/web",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
