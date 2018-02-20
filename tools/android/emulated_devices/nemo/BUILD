#Description:
#   Android Wear Nemo device.
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# TODO: Arm emulators are timing out.

make_device(
    name = "nemo",
    boot_properties = {
        "ro.emulator.circular": "true",
        "ro.product.manufacturer": "LG",
        "ro.product.model": "LG Watch Urbane 2nd Edition",
        "ro.product.name": "nemo",
    },
    cache = 32,
    emulator_types = [
        "qemu",
        "qemu2",
    ],
    horizontal_resolution = 480,
    max_api = 25,
    min_api = 20,
    ram = 512,
    screen_density = 348,
    system_image_flavors = ["wear"],
    vertical_resolution = 480,
    vm_heap = 48,
)
