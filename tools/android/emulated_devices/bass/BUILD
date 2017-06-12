#Description:
#   Android Wear Bass device.
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# TODO: Arm emulators are timing out.

make_device(
    name = "bass",
    boot_properties = {
        "ro.emulator.circular": "true",
        "ro.product.manufacturer": "LG",
        "ro.product.model": "LG Watch Urbane",
        "ro.product.name": "bass",
    },
    cache = 32,
    # TODO(b/30588599): Remove this once uma emulators can be built.
    emulator_types = [
        "qemu",
        "qemu2",
    ],
    horizontal_resolution = 320,
    min_api = 20,
    ram = 512,
    screen_density = 246,
    system_image_flavors = ["wear"],
    vertical_resolution = 320,
    vm_heap = 48,
)
