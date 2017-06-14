#Description:
#   Android Wear Carp device.
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# TODO: Arm emulators are timing out.

make_device(
    name = "carp",
    boot_properties = {
        "ro.emulator.circular": "true",
        "ro.emu.win_outset_bottom_px": 35,
        "ro.product.manufacturer": "Motorola",
        "ro.product.model": "Moto 360 v2 (small)",
        "ro.product.name": "carp",
    },
    cache = 32,
    # TODO(b/30588599): Remove this once uma emulators can be built.
    emulator_types = [
        "qemu",
        "qemu2",
    ],
    horizontal_resolution = 360,
    min_api = 22,
    ram = 512,
    screen_density = 280,
    system_image_flavors = [
        "wear",
        "wear-le",
    ],
    vertical_resolution = 325,
    vm_heap = 48,
)
