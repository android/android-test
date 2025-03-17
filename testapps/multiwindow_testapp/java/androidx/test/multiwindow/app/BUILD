# -*- mode: python; -*-
#
# Description:
#  Test App used for testing API integration tests in multi-window mode.

load("@build_bazel_rules_android//android:rules.bzl", "android_binary", "android_library")
load("//build_extensions:jetify.bzl", "jetify_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
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
        "//opensource/androidx:annotation",
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_window_window_core",
    ],
)

android_library(
    name = "lib_exported",
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    exports = ["lib"],
)

android_binary(
    name = "testapp",
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
