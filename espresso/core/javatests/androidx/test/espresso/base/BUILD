# Description:
#   Tests for espresso base classes

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_library(
    name = "util",
    testonly = 1,
    srcs = [
        "CoordinatesUtil.java",
        "OnDemandIdlingResource.java",
    ],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
    ],
)

axt_android_library_test(
    name = "AssertionErrorHandlerTest",
    srcs =
        ["AssertionErrorHandlerTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//ext/junit",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "AsyncTaskPoolMonitorTest",
    srcs =
        ["AsyncTaskPoolMonitorTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//ext/junit",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "DefaultFailureHandlerTest",
    srcs =
        ["DefaultFailureHandlerTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//services/storage",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "EspressoExceptionHandlerTest",
    srcs =
        ["EspressoExceptionHandlerTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//ext/junit",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "EventInjectorTest",
    srcs =
        ["EventInjectorTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//ext/junit",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "IdlingResourceRegistryTest",
    srcs =
        ["IdlingResourceRegistryTest.java"],
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
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//runner/rules",
        "//services/storage",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:javax_inject_javax_inject",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "PerformExceptionHandlerTest",
    srcs =
        ["PerformExceptionHandlerTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//ext/junit",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "ThrowableHandlerTest",
    srcs =
        ["ThrowableHandlerTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//ext/junit",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "UiControllerImplIntegrationTest",
    srcs =
        ["UiControllerImplIntegrationTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//espresso/core/java/androidx/test/espresso/base:idling_resource_registry",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:javax_inject_javax_inject",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "UiControllerImplTest",
    srcs =
        ["UiControllerImplTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//espresso/core/java/androidx/test/espresso/base:idling_resource_registry",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:javax_inject_javax_inject",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "ViewFinderImplTest",
    srcs =
        ["ViewFinderImplTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//ext/junit",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:javax_inject_javax_inject",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "ViewHierarchyExceptionHandlerTest",
    srcs =
        ["ViewHierarchyExceptionHandlerTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
