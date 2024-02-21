# -*- mode: python; -*-
#
# Description:
#  Test App used for UI Testing Lib integration tests.

load("//build_extensions:jetify.bzl", "jetify_android_binary", "jetify_android_library")

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
    jetify_sources = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        "//opensource/androidx:annotation",
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_multidex_multidex",
        "@maven//:androidx_recyclerview_recyclerview",
        "@maven//:com_google_android_material_material",
        "@maven//:com_google_guava_guava",
    ],
)

jetify_android_library(
    name = "lib_exported",
    testonly = 1,
    exports_manifest = 1,
    jetify_sources = True,
    manifest = "AndroidManifest.xml",
    exports = [
        ":lib",
    ],
)

jetify_android_library(
    name = "lib_neverlink",
    testonly = 1,
    jetify_sources = True,
    # No other libraries should depend on this one
    neverlink = 1,
    exports = [":lib"],
)

jetify_android_binary(
    name = "testapp",
    testonly = 1,
    assets = glob(["assets/**"]),
    assets_dir = "assets",
    jetify_sources = True,
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
