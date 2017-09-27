#Description:
#   Android Wear Pike device.
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# TODO: Arm emulators are timing out.

make_device(
    name = "pike",
    boot_properties = {
        "ro.emulator.circular": "false",
        "ro.product.manufacturer": "Polar",
        "ro.product.model": "Polar M600",
        "ro.product.name": "pike",
    },
    cache = 32,
    # TODO(b/30588599): Remove this once uma emulators can be built.
    emulator_types = [
        "qemu",
        "qemu2",
    ],
    horizontal_resolution = 240,
    min_api = 20,
    ram = 512,
    # using logical density instead of physical density as physical density (261) doesn't match
    # actual device rendering.
    screen_density = 240,
    system_image_flavors = ["wear"],
    vertical_resolution = 240,
    vm_heap = 48,
)
