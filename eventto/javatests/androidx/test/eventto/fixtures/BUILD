load("@build_bazel_rules_android//android:rules.bzl", "android_library")

android_library(
    name = "fixtures",
    srcs = glob(["*.java"]),
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    visibility = [
        "//eventto/javatests/androidx/test/eventto:__subpackages__",
    ],
    deps = [
    ],
)
