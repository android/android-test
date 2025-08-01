# Description:
#   Test App used for Multi-process UI Testing Lib integration tests

load("@build_bazel_rules_android//android:rules.bzl", "android_binary")
load("//build_extensions:jetify.bzl", "jetify_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = [
        "//opensource/visibility:testing_pkg",
        "//testapps/multiprocess_testapp/javatests/androidx/test/multiprocess/app:__pkg__",
    ],
)

# Libraries
# =========================================================

filegroup(
    name = "resource_files",
    srcs = glob(["res/**"]),
)

jetify_android_library(
    name = "lib",
    srcs =
        # get .java files in this dir
        glob(["**/*.java"]),
    assets = glob(["assets/**"]),
    assets_dir = "assets",
    manifest = "AndroidManifest.xml",
    # compile time requirements
    resource_files = [":resource_files"],
    deps = [
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "//opensource/androidx:annotation",
    ],
)

# Applications
# =========================================================

android_binary(
    name = "testapp",
    manifest = "AndroidManifest.xml",
    deps = [
        ":lib",
    ],
)
