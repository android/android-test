load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

package(default_applicable_licenses = ["//:license"])

kt_android_library(
    name = "fixtures",
    srcs = glob(["*.kt"]),
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    visibility = ["//visibility:public"],
    deps = [
    ],
)
