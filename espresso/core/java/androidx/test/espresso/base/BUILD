# Description:
#  Base package for espresso.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
    default_visibility = ["//espresso/core/java/androidx/test/espresso:espresso_pkg"],
)

android_library(
    name = "base",
    testonly = 0,
    srcs = glob(
        ["*.java"],
        exclude = [
            "AssertionErrorHandler.java",
            "DefaultFailureHandler.java",
            "EspressoExceptionHandler.java",
            "PerformExceptionHandler.java",
            "ThrowableHandler.java",
            "IdleNotifier.java",
            "IdlingUiController.java",
            "IdlingResourceRegistry.java",
            "Interrogator.java",
            "LooperIdlingResourceInterrogationHandler.java",
            "TestLooperManagerCompat.java",
            "ViewHierarchyExceptionHandler.java",
        ],
    ),
    deps = [
        ":default_failure_handler",
        ":idling_resource_registry",
        ":idling_uicontroller_interface",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/internal/inject",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/core/java/androidx/test/espresso/util/concurrent",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource",
        "//opensource/androidx:annotation",
        "//opensource/dagger",
        "//runner/android_junit_runner",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

android_library(
    name = "active_root_lister",
    srcs = ["ActiveRootLister.java"],
    visibility = [
        "//espresso/core/java/androidx/test/espresso:espresso_first_party",
    ],
    deps = [
        "//espresso/core/java/androidx/test/espresso:interface",
    ],
)

android_library(
    name = "idling_resource_registry",
    testonly = 0,
    srcs = [
        "IdleNotifier.java",
        "IdlingResourceRegistry.java",
        "Interrogator.java",
        "LooperIdlingResourceInterrogationHandler.java",
        "TestLooperManagerCompat.java",
    ],
    deps = [
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "idling_uicontroller_interface",
    testonly = 0,
    srcs = ["IdlingUiController.java"],
    deps = [
        ":idling_resource_registry",
        "//espresso/core/java/androidx/test/espresso:interface",
    ],
)

android_library(
    name = "default_failure_handler",
    testonly = 0,
    srcs = [
        "AssertionErrorHandler.java",
        "DefaultFailureHandler.java",
        "EspressoExceptionHandler.java",
        "PerformExceptionHandler.java",
        "ThrowableHandler.java",
        "ViewHierarchyExceptionHandler.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//core/java/androidx/test/core:core_not_test_only",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/internal/inject",
        "//espresso/core/java/androidx/test/espresso/util",
        "//opensource/androidx:annotation",
        "//opensource/dagger",
        "//runner/monitor",
        "//services/storage/java/androidx/test/services/storage",
        "@maven//:javax_inject_javax_inject",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
