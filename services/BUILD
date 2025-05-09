load("@build_bazel_rules_android//android:rules.bzl", "android_binary")
load("@rules_license//rules:license.bzl", "license")
load("//build_extensions:axt_versions.bzl", "SERVICES_VERSION")
load("//build_extensions/maven:axt_maven_apk.bzl", "axt_maven_apk")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")

# Description:

licenses(["notice"])

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
    default_visibility = [
        "//:__subpackages__",
    ],
)

license(
    name = "license",
)

exports_files(["LICENSE"])

# Standard test services that can be installed on any device.
android_binary(
    name = "test_services",
    custom_package = "androidx.test.services",
    main_dex_proguard_specs = ["//build_extensions:mainDexClasses.rules"],
    manifest = "AndroidManifest.xml",
    manifest_values = {
        "appVersion": SERVICES_VERSION,
    },
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

axt_maven_apk(
    name = "test_services_release_apk",
    included_dep = ":test_services",
)

maven_artifact(
    name = "test_services_maven_artifact",
    last_updated = "20191210000000",
    target = ":test_services_release_apk",
)
