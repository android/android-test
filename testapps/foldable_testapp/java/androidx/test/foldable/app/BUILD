# -*- mode: python; -*-
#
# Description:
#  Test App used for foldable testing API integration tests.

load("@build_bazel_rules_android//android:rules.bzl", "android_binary")
load("//build_extensions:jetify.bzl", "jetify_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = [
        "//opensource/visibility:testing_pkg",
    ],
)

jetify_android_library(
    name = "lib",
    srcs = glob(
        ["**/*.java"],
    ),
    assets = glob(["assets/**"]),
    assets_dir = "assets",
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_window_window",
        "@maven//:androidx_window_window_java",
    ],
)

android_binary(
    name = "testapp",
    testonly = 1,
    assets = glob(["assets/**"]),
    assets_dir = "assets",
    manifest = "AndroidManifest.xml",
    multidex = "legacy",
    resource_files = glob(["res/**"]),
    deps = [
        ":lib",
        "//core:manifest",
        "@maven//:androidx_multidex_multidex",
    ],
)
