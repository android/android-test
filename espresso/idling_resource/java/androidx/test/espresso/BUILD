# Description:
#   Light weight module represents a resources of an application under test which can cause
#   asynchronous background work to happen during test execution.

load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ESPRESSO_VERSION")
load("//build_extensions:combine_jars.bzl", "combine_jars")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

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

# rule for outputting externally released binary. Produces
# espresso_idling_resource_release_no_deps.jar and espresso_idling_resource_release.aar
axt_release_lib(
    name = "espresso_idling_resource_release",
    custom_package = "androidx.test.espresso.idling.resource",
    keep_spec = "androidx/test/espresso",
    remove_spec = "androidx/test/espresso/idling/resource/R[$$\\.]",
    deps = [
        ":idling_resource",
    ],
)

filegroup(
    name = "idling_resource_src",
    srcs = [
        #     ":libcounting_idling_resource-src.jar",
        ":libidling_resource_internal-src.jar",
    ],
)

combine_jars(
    name = "idling_resource_src_jars",
    srcs = [
        ":idling_resource_src",
    ],
)

dackka_test(
    name = "espresso_idling_resource_doc",
    jar = ":espresso_idling_resource_release_all_deploy.jar",
    src_jar = ":idling_resource_src_jars.jar",
)

maven_artifact(
    name = "espresso_idling_resource_maven_artifact",
    src = ":espresso_idling_resource_release.aar",
    artifact_id = "espresso-idling-resource",
    group_id = "androidx.test.espresso",
    last_updated = "20170622000000",
    src_jar = ":idling_resource_src_jars.jar",
    version = "%s" % ESPRESSO_VERSION,
)

api_checks(
    name = "espresso_idling_resource_api",
    deploy_jar = ":espresso_idling_resource_release_all_deploy.jar",
    src_jar = ":idling_resource_src_jars.jar",
)
