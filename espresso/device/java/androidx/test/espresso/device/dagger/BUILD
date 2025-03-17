# Description:
#  Dagger components for device.

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
    default_visibility = ["//espresso/device/java/androidx/test/espresso/device:__subpackages__"],
)

kt_android_library(
    name = "dagger",
    srcs = glob(["*.kt"]),
    deps = [
        "//espresso/device/java/androidx/test/espresso/device/common",
        "//espresso/device/java/androidx/test/espresso/device/controller",
        "//espresso/device/java/androidx/test/espresso/device/controller:controller_impl",
        "//espresso/device/java/androidx/test/espresso/device/controller/emulator",
        "//opensource/androidx:annotation",
        "//opensource/dagger",
        "//runner/monitor",
    ],
)
