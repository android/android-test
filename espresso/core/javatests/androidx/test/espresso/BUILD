# Description:
#   Tests for espresso core

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load(
    "//build_extensions:axt_android_application_test.bzl",
    "axt_android_application_test",
)

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

licenses(["notice"])

android_library(
    name = "utils",
    srcs = [
        "StringPatternMatcher.java",
        "TestTracer.java",
    ],
    visibility = [
        "//espresso/core/javatests/androidx/test/espresso:__subpackages__",
    ],
    deps = [
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "AmbiguousViewMatcherExceptionTest",
    srcs = ["AmbiguousViewMatcherExceptionTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "AppNotIdleExceptionTest",
    srcs = ["AppNotIdleExceptionTest.java"],
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "EspressoEdgeCaseTest",
    srcs = ["EspressoEdgeCaseTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_application_test(
    name = "EspressoTest",
    srcs = ["EspressoTest.java"],
    instruments = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "InteractionResultsHandlerTest",
    srcs = ["InteractionResultsHandlerTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/util/concurrent",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)

axt_android_library_test(
    name = "NoMatchingViewExceptionTest",
    srcs = ["NoMatchingViewExceptionTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "OnIdleTest",
    srcs = ["OnIdleTest.java"],
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "ViewInteractionTest",
    srcs = ["ViewInteractionTest.java"],
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/internal/data",
        "//espresso/core/java/androidx/test/espresso/remote:bindable",
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/util/concurrent",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)
