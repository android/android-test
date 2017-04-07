#Description:
#   Android Wear Platina device.
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# TODO: Arm emulators are timing out.

make_device(
    name = "platina",
    boot_properties = {
        "ro.product.manufacturer": "LG",
        "ro.product.model": "LG G Watch",
        "ro.product.name": "platina",
    },
    cache = 32,
    # TODO(b/30588599): Remove this once uma emulators can be built.
    emulator_types = ["qemu"],
    horizontal_resolution = 280,
    max_api = 25,
    min_api = 20,
    ram = 512,
    screen_density = 240,
    system_image_flavors = ["wear"],
    vertical_resolution = 280,
    vm_heap = 48,
)
