# Description:
#   EspressoDevice - the new Synchronized Device State Change API for Android.

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/device/java/androidx/test/espresso/device:device_pkg",
    ],
)

exports_files(["AndroidManifest.xml"])

# Add only device packages here.
package_group(
    name = "device_pkg",
    packages = ["//espresso/device/..."],
)

kt_android_library(
    name = "device",
    testonly = 1,
    exports = [
        ":device_api",
        "//espresso/device/java/androidx/test/espresso/device/action",
        "//espresso/device/java/androidx/test/espresso/device/filter",
        "//espresso/device/java/androidx/test/espresso/device/rules",
        "//espresso/device/java/androidx/test/espresso/device/sizeclass",
    ],
)

kt_android_library(
    name = "device_api",
    testonly = 1,
    srcs = glob(["*.kt"]),
    deps = [
        "//annotation/java/androidx/test/annotation",
        "//espresso/device/java/androidx/test/espresso/device/action",
        "//espresso/device/java/androidx/test/espresso/device/context",
        "//espresso/device/java/androidx/test/espresso/device/dagger",
        "//espresso/device/java/androidx/test/espresso/device/sizeclass",
        "//opensource/dagger",
        "//runner/monitor",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "device_aar_lib",
    testonly = 1,
    manifest = "AndroidManifest.xml",
    exports = [
        "//espresso/device/java/androidx/test/espresso/device/filter:requires_display_filter",
        "//espresso/device/java/androidx/test/espresso/device/sizeclass",
    ],
)

# Currently, only the @RequiresDisplay test filter and size classes are included in external release.
axt_android_aar(
    name = "device_aar",
    testonly = 1,
    expected_class_prefixes = [
        "androidx.test.espresso.device",
    ],
    included_dep = ":device_aar_lib",
    visibility = [
        "//:__pkg__",
    ],
)

maven_artifact(
    name = "device_maven_artifact",
    testonly = 1,
    last_updated = "20220825000000",
    target = ":device_aar",
    visibility = ["//visibility:public"],
)

dackka_test(
    name = "device_doc",
    runtime_dep = ":device_aar_lib",
    src_jar = ":device_aar-src.jar",
)

api_checks(
    name = "device_api_check",
    runtime_dep = ":device_aar_lib",
    src_jar = ":device_aar-src.jar",
)
