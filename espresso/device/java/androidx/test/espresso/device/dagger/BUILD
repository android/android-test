# Description:
#  Dagger components for device.

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/device/java/androidx/test/espresso/device:__subpackages__"],
)

kt_android_library(
    name = "dagger",
    testonly = 1,
    srcs = glob(["*.kt"]),
    deps = [
        "//espresso/device/java/androidx/test/espresso/device/common",
        "//espresso/device/java/androidx/test/espresso/device/context",
        "//espresso/device/java/androidx/test/espresso/device/context:context_impl",
        "//espresso/device/java/androidx/test/espresso/device/controller",
        "//espresso/device/java/androidx/test/espresso/device/controller:controller_impl",
        "//espresso/device/java/androidx/test/espresso/device/controller/emulator",
        "//opensource/dagger",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
    ],
)
