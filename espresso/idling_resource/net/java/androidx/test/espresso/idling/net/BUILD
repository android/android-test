load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# Description:
# Network Idling Resources
licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

android_library(
    name = "uri_idling_resource",
    srcs = ["UriIdlingResource.java"],
    manifest = "AndroidManifest.xml",
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "@maven//:androidx_annotation_annotation",
    ],
)

# rule for outputting externally released binary.
axt_android_aar(
    name = "idling_net_aar",
    expected_class_prefixes = [
        "androidx.test.espresso.idling.net",
    ],
    included_dep = ":uri_idling_resource",
)

maven_artifact(
    name = "idling_net_maven_artifact",
    last_updated = "20170622000000",
    target = ":idling_net_aar",
)

dackka_test(
    name = "idling_net_doc",
    runtime_dep = ":uri_idling_resource",
    src_jar = ":idling_net_aar-src.jar",
)

api_checks(
    name = "idling_net_api",
    runtime_dep = ":uri_idling_resource",
    src_jar = ":idling_net_aar-src.jar",
)
