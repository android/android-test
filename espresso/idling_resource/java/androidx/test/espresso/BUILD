# Description:
#   Light weight module represents a resources of an application under test which can cause
#   asynchronous background work to happen during test execution.

load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

licenses(["notice"])

IDLING_INTERFACE = [
    "IdlingResource.java",
    "IdlingRegistry.java",
    "idling/CountingIdlingResource.java",
]

java_library(
    name = "idling_resource_internal",
    srcs = IDLING_INTERFACE,
    visibility = [
        "//:__subpackages__",
        "//espresso/core/java/androidx/test/espresso:espresso_pkg",
    ],
    deps = ["//opensource:androidsdk_compiletime"],
)

alias(
    name = "idling_resource",
    actual = "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
)

alias(
    name = "counting_idling_resource",
    actual = "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
)

alias(
    name = "scheduled_thread_pool_idling_resource",
    actual = "//espresso/idling_resource/concurrent/java/androidx/test/espresso/idling/concurrent:scheduled_thread_pool_idling_resource",
)

alias(
    name = "thread_pool_idling_resource",
    actual = "//espresso/idling_resource/concurrent/java/androidx/test/espresso/idling/concurrent:thread_pool_idling_resource",
)

android_library(
    name = "idling_resource_aar_lib",
    custom_package = "androidx.test.espresso.idling.resource",
    manifest = "AndroidManifest.xml",
    exports = [":idling_resource_internal"],
)

# rule for outputting externally released binary.
axt_android_aar(
    name = "idling_resource_aar",
    expected_class_prefixes = [
        "androidx.test.espresso.Idling",
        "androidx.test.espresso.idling",
    ],
    included_dep = ":idling_resource_aar_lib",
)

maven_artifact(
    name = "espresso_idling_resource_maven_artifact",
    last_updated = "20170622000000",
    target = ":idling_resource_aar",
)

dackka_test(
    name = "espresso_idling_resource_doc",
    runtime_dep = ":idling_resource_aar_lib",
    src_jar = ":idling_resource_aar-src.jar",
)

api_checks(
    name = "espresso_idling_resource_api",
    runtime_dep = ":idling_resource_aar_lib",
    src_jar = ":idling_resource_aar-src.jar",
)
