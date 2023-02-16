# Description:
#   EspressoDevice - the new Synchronized Device State Change API for Android.

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:combine_jars.bzl", "combine_jars")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_ANNOTATION_VERSION", "ANNOTATION_VERSION", "ESPRESSO_DEVICE_VERSION", "KOTLIN_VERSION", "MONITOR_VERSION", "RUNNER_VERSION")

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
        "@jsr330",
    ],
)

combine_jars(
    name = "device_src_jars",
    srcs = [
        "//espresso/device/java/androidx/test/espresso/device/filter:libfilter-src.jar",
        "//espresso/device/java/androidx/test/espresso/device/sizeclass:libsizeclass-src.jar",
    ],
)

# Currently, only the @RequiresDisplay test filter and size classes are included in external release.
axt_release_lib(
    name = "device_release",
    custom_package = "androidx.test.espresso.device",
    keep_spec = "androidx/test/espresso/device/.*|META-INF/androidx.test.espresso.device.*.kotlin_module",
    proguard_specs = [
        "//:proguard_binary.cfg",
        "//espresso:proguard_binary.cfg",
    ],
    remove_spec = "androidx/test/espresso/device/R[$$\\.]",
    deps = [
        "//espresso/device/java/androidx/test/espresso/device/filter:requires_display_filter",
        "//espresso/device/java/androidx/test/espresso/device/sizeclass",
    ],
)

maven_artifact(
    name = "device_maven_artifact",
    src = "device_release.aar",
    artifact_deps = [
        "androidx.annotation:annotation:%s" % ANDROIDX_ANNOTATION_VERSION,
        "androidx.test:monitor:[%s]" % MONITOR_VERSION,
        "androidx.test:runner:%s" % RUNNER_VERSION,
        "org.jetbrains.kotlin:kotlin-stdlib:%s" % KOTLIN_VERSION,
        "androidx.test:annotation:%s" % ANNOTATION_VERSION,
    ],
    artifact_id = "espresso-device",
    group_id = "androidx.test.espresso",
    last_updated = "20220825000000",
    src_jar = ":device_src_jars.jar",
    version = "%s" % ESPRESSO_DEVICE_VERSION,
    visibility = [
        "//:__pkg__",
    ],
)
