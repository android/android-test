# Description:
#   Generic watch runs all available API's for Wear (which is API 20 onwards).
#
#   target name format:
#     :wear_${api_level}_${arch} for vanilla android

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "generic_watch",
    boot_properties = {
        "ro.scuba.model": "Generic Watch",
    },
    cache = 32,
    # TODO(b/30588599): Remove this once uma emulators can be built.
    emulator_types = [
        "qemu",
        "qemu2",
    ],
    horizontal_resolution = 400,
    min_api = 20,
    ram = 512,
    screen_density = 286,
    system_image_flavors = ["wear"],
    vertical_resolution = 400,
    vm_heap = 48,
)
