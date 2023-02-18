# Description:
#   Light weight module that contains IdlinResources around concurrent Java APIs

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ESPRESSO_VERSION")
load("//build_extensions:combine_jars.bzl", "combine_jars")
load("//build_extensions:release.bzl", "axt_release_lib")
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

# rule for outputting externally released binary. Produces
# idling_concurrent_release_no_deps.jar and idling_concurrent_release.aar
axt_release_lib(
    name = "idling_concurrent_release",
    keep_spec = "androidx/test/espresso/idling/concurrent",
    remove_spec = "androidx/test/espresso/idling/concurrent/R[$$\\.]",
    deps = [
        ":idling_concurrent",
    ],
)

filegroup(
    name = "idling_concurrent_src",
    srcs = [
        ":libidling_concurrent-src.jar",
    ],
)

combine_jars(
    name = "idling_concurrent_src_jars",
    srcs = [
        ":idling_concurrent_src",
    ],
)

dackka_test(
    name = "idling_concurrent_doc",
    jar = ":idling_concurrent_release_all_deploy.jar",
    src_jar = ":idling_concurrent_src_jars.jar",
)

maven_artifact(
    name = "idling_concurrent_maven_artifact",
    src = ":idling_concurrent_release.aar",
    artifact_deps = [
        "androidx.test.espresso:espresso-idling-resource:%s" % ESPRESSO_VERSION,
    ],
    artifact_id = "idling-concurrent",
    group_id = "androidx.test.espresso.idling",
    last_updated = "20170622000000",
    src_jar = ":idling_concurrent_src_jars.jar",
    version = "%s" % ESPRESSO_VERSION,
)

api_checks(
    name = "idling_concurrent_api",
    deploy_jar = ":idling_concurrent_release_all_deploy.jar",
    src_jar = ":idling_concurrent_src_jars.jar",
)
