load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
    default_visibility = ["//services:__subpackages__"],
)

licenses(["notice"])

# Content providers that provide read write or read only access to the SDCard.
android_library(
    name = "storage_content_providers",
    srcs = glob(["*.java"]),
    manifest = "AndroidManifest.xml",
    deps = [
        "//runner/monitor",
        "//services/storage",
        "//services/storage/java/androidx/test/services/storage:storage_service_pb_java_proto_lite",
        "//services/storage/java/androidx/test/services/storage:test_storage_constants",
        "//services/storage/java/androidx/test/services/storage/file",
    ],
)
