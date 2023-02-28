# Description:
#   Tests for espresso contrib classes

load("//build_extensions:jetify.bzl", "jetify_android_library")
load(
    "//build_extensions:android_app_instrumentation_tests.bzl",
    "android_app_instrumentation_tests",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])
## Android tests

UTIL_SRC = ["RecyclerViewIntegrationTest.java"]

jetify_android_library(
    name = "util",
    testonly = 1,
    srcs = UTIL_SRC,
    deps = [
        "//core",
        "//espresso/contrib/java/androidx/test/espresso/contrib:recycler_view",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:androidx_recyclerview_recyclerview",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    srcs = glob(
        ["*.java"],
        exclude = UTIL_SRC,
    ),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    library_args = {
        "manifest": "AndroidManifest.xml",
    },
    deps = [
        ":util",
        "//core",
        "//espresso/contrib/java/androidx/test/espresso/contrib:accessibility_checks",
        "//espresso/contrib/java/androidx/test/espresso/contrib:activity_result_matchers",
        "//espresso/contrib/java/androidx/test/espresso/contrib:design_lib_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:recycler_view",
        "//espresso/contrib/java/androidx/test/espresso/contrib:view_pager_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:uri_idling_resource",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
        "@maven//:androidx_core_core",
        "@maven//:androidx_recyclerview_recyclerview",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)
