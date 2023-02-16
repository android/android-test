load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//services:license"],
    default_visibility = [
        "//services:__subpackages__",
        "//services/speakeasy/javatests/androidx/test/services/speakeasy/server:__pkg__",
    ],
)

licenses(["notice"])

android_library(
    name = "speak_easy_core",
    srcs = ["SpeakEasy.java"],
    deps = [
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "speak_easy_service",
    srcs = [
        "SpeakEasyContentProvider.java",
        "SpeakEasyService.java",
    ],
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    visibility = [
        "//services:__subpackages__",
        "//services/speakeasy/javatests/androidx/test/services/speakeasy:__subpackages__",
    ],
    deps = [
        ":speak_easy_core",
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
        "@maven//:androidx_core_core",
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "server",
    exports = [":speak_easy_service"],
)
