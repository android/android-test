# Description: Build rules for building androidx.test.annotation from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

# all users should reference the equivalent targets in //annotation
package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//:__subpackages__"],
)

licenses(["notice"])

android_library(
    name = "annotation",
    srcs = glob(
        ["**/*.java"],
    ),
    manifest = "AndroidManifest.xml",
    tags = ["alt_dep=//annotation"],
    deps = [
        "//opensource/androidx:annotation",
        "@maven//:androidx_annotation_annotation_experimental",
    ],
)
