# Description:
#   Tests for Espresso Intents lib.

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load(
    "//build_extensions:phone_devices.bzl",
    "devices",
)

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "RuntimePermissionStubberIntegrationTest",
    srcs = ["RuntimePermissionStubberIntegrationTest.java"],
    args = ["--clear_package_data"],
    device_list = devices(),
    manifest = "AndroidManifest.xml",
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "IntentsIntegrationTest",
    srcs = ["IntentsIntegrationTest.java"],

    # don't run on slim device, since IntentsIntegrationTest depends on intents resolving to specific apps like com.android.contacts
    # which are not present on slim image
    device_list = devices(use_slim = False),
    manifest = "AndroidManifest.xml",
    shard_count = 3,
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "IntentsTest",
    srcs = ["IntentsTest.java"],
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
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "ResettingStubberImplTest",
    srcs = ["ResettingStubberImplTest.java"],
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
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
