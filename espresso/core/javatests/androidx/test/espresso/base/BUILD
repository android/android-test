# Description:
#   Tests for espresso base classes

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:android_app_instrumentation_tests.bzl",
    "android_app_instrumentation_tests",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

UTIL_SRC = [
    "CoordinatesUtil.java",
    "OnDemandIdlingResource.java",
]

android_library(
    name = "util",
    testonly = 1,
    srcs = UTIL_SRC,
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
    ],
)

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    srcs = glob(
        ["*.java"],
        exclude = UTIL_SRC,
    ),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//espresso/core/java/androidx/test/espresso/base:idling_resource_registry",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/javatests/androidx/test/espresso:utils",
        "//ext/junit",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//runner/rules",
        "//services/storage",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_truth_truth",
        "@maven//:javax_inject_javax_inject",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)
