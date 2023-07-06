# Description:
#   Tests for the sdcard data content providers

load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
)

package(default_applicable_licenses = ["//services:license"])

licenses(["notice"])

axt_android_library_test(
    name = "TestArgsContentProviderTest",
    srcs = ["TestArgsContentProviderTest.java"],
    args = [
        "--install_test_services=True",
    ],
    device_list = devices(apis(min_api = 19)),
    manifest = "AndroidManifest.xml",
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "//services/storage/java/androidx/test/services/storage:storage_service_pb_java_proto_lite",
        "//services/storage/java/androidx/test/services/storage:test_storage_constants",
        "//services/storage/java/androidx/test/services/storage/file",
        "//services/storage/java/androidx/test/services/storage/provider:storage_content_providers",
        "@androidsdk//:legacy_test-30",
        "@maven//:com_google_guava_guava",
    ],
)

axt_android_library_test(
    name = "TestFileContentProviderTest",
    srcs = ["TestFileContentProviderTest.java"],
    args = [
        "--install_test_services=True",
    ],
    device_list = devices(apis(min_api = 19)),
    manifest = "AndroidManifest.xml",
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "//services/storage/java/androidx/test/services/storage:storage_service_pb_java_proto_lite",
        "//services/storage/java/androidx/test/services/storage:test_storage_constants",
        "//services/storage/java/androidx/test/services/storage/file",
        "//services/storage/java/androidx/test/services/storage/provider:storage_content_providers",
        "@androidsdk//:legacy_test-30",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
    ],
)
