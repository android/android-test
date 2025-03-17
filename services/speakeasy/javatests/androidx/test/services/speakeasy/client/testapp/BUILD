load("@build_bazel_rules_android//android:rules.bzl", "android_binary")

licenses(["notice"])

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
    default_visibility = [
        "//services/speakeasy/javatests/androidx/test/services/speakeasy:__subpackages__",
    ],
)

android_binary(
    name = "testapp",
    manifest = "AndroidManifest.xml",
    multidex = "legacy",
    deps = [
        "//services/speakeasy/java/androidx/test/services/speakeasy/server:speak_easy_service",
        "@maven//:androidx_multidex_multidex",
    ],
)
