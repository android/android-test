# Description:
#   Exposes sd card storage to tests regardless of permissions.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

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
java_library(
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

axt_android_aar(
    name = "test_storage_aar",
    expected_class_prefixes = [
        "androidx.test.services.storage",
    ],
    included_dep = ":storage",
)

maven_artifact(
    name = "test_storage_maven_artifact",
    last_updated = "2020020700000",
    target = ":test_storage_aar",
)

dackka_test(
    name = "test_storage_doc",
    runtime_dep = ":storage",
    src_jar = ":test_storage_aar-src.jar",
)

api_checks(
    name = "storage_api",
    runtime_dep = ":storage",
    src_jar = ":test_storage_aar-src.jar",
)
