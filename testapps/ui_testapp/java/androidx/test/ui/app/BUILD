# -*- mode: python; -*-
#
# Description:
#  Test App used for UI Testing Lib integration tests.

load("//build_extensions:jetify.bzl", "jetify_android_library")
load("@build_bazel_rules_android//android:rules.bzl", "android_binary", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = [
        "//opensource/visibility:testing_pkg",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:__pkg__",
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
        "@maven//:androidx_recyclerview_recyclerview",
        "@maven//:com_google_android_material_material",
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "lib_exported",
    testonly = 1,
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    exports = [":lib"],
)

android_library(
    name = "lib_neverlink",
    testonly = 1,
    # No other libraries should depend on this one
    neverlink = 1,
    exports = [":lib"],
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
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_multidex_multidex",
    ],
)
