load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//:license"])

android_library(
    name = "fixtures",
    srcs = glob(["*.kt"]),
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    visibility = ["//visibility:public"],
    deps = [
    ],
)
