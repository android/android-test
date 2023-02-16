load("@rules_license//rules:license.bzl", "license")
load("@build_bazel_rules_android//android:rules.bzl", "android_binary")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "SERVICES_VERSION")
load("//build_extensions:combine_jars.bzl", "combine_jars")

# Description:

licenses(["notice"])

package(
    default_applicable_licenses = ["//services:license"],
    default_visibility = [
        "//:__subpackages__",
    ],
)

license(name = "license")

exports_files(["LICENSE"])

# Standard test services that can be installed on any device.
android_binary(
    name = "test_services",
    custom_package = "androidx.test.services",
    licenses = ["permissive"],
    manifest = "AndroidManifest.xml",
    multidex = "legacy",  # to support android_fix_libs
    proguard_specs = [
        "//:proguard_binary.cfg",
        "proguard_library.cfg",
    ],
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "//services/shellexecutor:exec_server",
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
        "//services/speakeasy/java/androidx/test/services/speakeasy/server",
        "//services/storage/java/androidx/test/services/storage/provider:storage_content_providers",
        "@maven//:androidx_multidex_multidex",
    ],
)

combine_jars(
    name = "test_services_jars",
    srcs = [
        "//services/shellexecutor:shellexecuter_src",
        "//services/speakeasy/java/androidx/test/services/speakeasy:libprotocol-src.jar",
        "//services/speakeasy/java/androidx/test/services/speakeasy/server:libspeak_easy_service-src.jar",
        "//services/storage/java/androidx/test/services/storage/provider:libstorage_content_providers-src.jar",
    ],
)

maven_artifact(
    name = "test_services_maven_artifact",
    src = ":test_services.apk",
    artifact_id = "test-services",
    group_id = "androidx.test.services",
    last_updated = "20191210000000",
    src_jar = ":test_services_jars.jar",
    version = "%s" % SERVICES_VERSION,
)
