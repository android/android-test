# Description:
#  Base package for espresso.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/core/java/androidx/test/espresso:espresso_pkg"],
)

android_library(
    name = "base",
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
        "//opensource/dagger",
        "//runner/android_junit_runner",
        "@maven//:androidx_annotation_annotation",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
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
    srcs = [
        "IdleNotifier.java",
        "IdlingResourceRegistry.java",
        "Interrogator.java",
        "LooperIdlingResourceInterrogationHandler.java",
    ],
    deps = [
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

android_library(
    name = "idling_uicontroller_interface",
    srcs = ["IdlingUiController.java"],
    deps = [
        ":idling_resource_registry",
        "//espresso/core/java/androidx/test/espresso:interface",
    ],
)

android_library(
    name = "default_failure_handler",
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
        "//opensource/dagger",
        "//runner/monitor",
        "//services/storage/java/androidx/test/services/storage",
        "@maven//:androidx_annotation_annotation",
        "@maven//:javax_inject_javax_inject",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
