# Description: Tests for espresso web

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
    "gmscore_channel",
)

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "JavaScriptExecutionActionTest",
    srcs = [
        "JavaScriptExecutionActionTest.java",
    ],

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
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
