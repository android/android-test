# Description:
#   Light weight module that contains IdlinResources around concurrent Java APIs

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

licenses(["notice"])

android_library(
    name = "scheduled_thread_pool_idling_resource",
    srcs = [
        "IdlingScheduledThreadPoolExecutor.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
    ],
)

android_library(
    name = "thread_pool_idling_resource",
    srcs = [
        "IdlingThreadPoolExecutor.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
    ],
)

android_library(
    name = "idling_concurrent",
    srcs = [
        "IdlingScheduledThreadPoolExecutor.java",
        "IdlingThreadPoolExecutor.java",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
    ],
)

# rule for outputting externally released binary.
axt_android_aar(
    name = "idling_concurrent_aar",
    expected_class_prefixes = [
        "androidx.test.espresso.idling.concurrent",
    ],
    included_dep = ":idling_concurrent",
)

maven_artifact(
    name = "idling_concurrent_maven_artifact",
    last_updated = "20170622000000",
    target = ":idling_concurrent_aar",
)

dackka_test(
    name = "idling_concurrent_doc",
    runtime_dep = ":idling_concurrent",
    src_jar = ":idling_concurrent_aar-src.jar",
)

api_checks(
    name = "idling_concurrent_api",
    runtime_dep = ":idling_concurrent",
    src_jar = ":idling_concurrent_aar-src.jar",
)
