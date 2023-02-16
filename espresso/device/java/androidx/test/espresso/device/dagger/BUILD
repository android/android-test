# Description:
#  Dagger components for device.

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

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
        "//espresso/device/java/androidx/test/espresso/device/context",
        "//espresso/device/java/androidx/test/espresso/device/context:context_impl",
        "//espresso/device/java/androidx/test/espresso/device/controller",
        "//espresso/device/java/androidx/test/espresso/device/controller:controller_impl",
        "//espresso/device/java/androidx/test/espresso/device/controller/emulator",
        "//espresso/device/java/androidx/test/espresso/device/util",
        "//runner/monitor",
        # TODO(b/215420259) fix gRPC dependency
        "//opensource/dagger",
    ],
)
