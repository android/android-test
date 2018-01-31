#Description:
#   Android Wear Narwhal device.
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# TODO: Arm emulators are timing out.

make_device(
    name = "narwhal",
    boot_properties = {
        "ro.emulator.circular": "true",
        "ro.product.manufacturer": "Google",
        "ro.product.name": "narwhal",
        "ro.display.obstruction.top": "152",
        "ro.display.obstruction.left": "152",
        "ro.display.obstruction.bottom": "208",
        "ro.display.obstruction.right": "208",
    },
    cache = 32,
    emulator_types = [
        "qemu2",
    ],
    horizontal_resolution = 360,
    min_api = 20,
    ram = 512,
    screen_density = 300,
    system_image_flavors = ["wear"],
    vertical_resolution = 360,
    vm_heap = 48,
)
