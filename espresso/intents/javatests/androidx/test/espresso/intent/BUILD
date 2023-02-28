# Description:
#   Tests for Espresso Intents lib.

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
    name = "RuntimePermissionStubberIntegrationTest",
    srcs = ["RuntimePermissionStubberIntegrationTest.java"],
    args = ["--clear_package_data"],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    device_list = devices(
        apis(max_api = 29),
    ),
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent:resolved_intent_interface",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "RuntimePermissionStubberIntegrationTest.java",
        ],
    ),
    args = ["--clear_package_data"],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    # don't run on slim device, since IntentsIntegrationTest depends on intents resolving to specific apps like com.android.contacts
    # which are not present on slim image
    device_list = devices(use_slim = False),
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent:resolved_intent_interface",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)
