# Description:
#   Exposes sd card storage to tests regardless of permissions.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_ANNOTATION_VERSION", "ANNOTATION_VERSION", "MONITOR_VERSION", "SERVICES_VERSION")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")
load("//build_extensions:combine_jars.bzl", "combine_jars")

package(
    default_applicable_licenses = ["//services:license"],
    default_visibility = [
        "//:__subpackages__",
    ],
)

licenses(["notice"])

android_library(
    name = "storage",
    srcs = [
        "TestStorage.java",
        "TestStorageException.java",
        "internal/TestStorageUtil.java",
        "internal/package-info.java",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        ":test_storage_constants",
        "//annotation",
        "//runner/monitor",
        "//services/storage/java/androidx/test/services/storage/file",
        "@maven//:androidx_annotation_annotation",
    ],
)

# Constants shared between on-device (android) and host-side (java code) testing
# infrastructure for the storage service.
android_library(
    name = "test_storage_constants",
    srcs = ["TestStorageConstants.java"],
    deps = [
        "//annotation",
    ],
)

# Proto libs for the storage service.
proto_library(
    name = "storage_service_pb",
    srcs = ["test_storage_service.proto"],
)

java_lite_proto_library(
    name = "storage_service_pb_java_proto_lite",
    deps = [":storage_service_pb"],
)

java_proto_library(
    name = "storage_service_pb_java_proto",
    deps = [":storage_service_pb"],
)

filegroup(
    name = "storage_src",
    srcs = [
        ":libstorage-src.jar",
        "//services/storage/java/androidx/test/services/storage/file:libfile-src.jar",
    ],
)

combine_jars(
    name = "storage_src_all_jars",
    srcs = [
        ":storage_src",
    ],
)

# Generate release artifacts for the test storage.
axt_release_lib(
    name = "test_storage_release",
    keep_spec = "androidx/test/services/storage",
    # remove the generated R class
    remove_spec = "androidx/test/services/storage/R[$$\\.]",
    deps = [
        ":storage",
    ],
)

dackka_test(
    name = "test_storage_doc",
    jar = ":test_storage_release_all_deploy.jar",
    src_jar = ":storage_src_all_jars.jar",
)

maven_artifact(
    name = "test_storage_maven_artifact",
    src = ":test_storage_release.aar",
    artifact_deps = [
        "androidx.annotation:annotation:%s" % ANDROIDX_ANNOTATION_VERSION,
        "androidx.test:monitor:[%s]" % MONITOR_VERSION,
        "androidx.test:annotation:%s" % ANNOTATION_VERSION,
    ],
    artifact_id = "storage",
    group_id = "androidx.test.services",
    last_updated = "2020020700000",
    src_jar = ":storage_src_all_jars.jar",
    version = "%s" % SERVICES_VERSION,
)

api_checks(
    name = "storage_api",
    deploy_jar = ":test_storage_release_all_deploy.jar",
    src_jar = ":storage_src_all_jars.jar",
)
