#Description:
#   Android Wear Koi device.
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# TODO: Arm emulators are timing out.

make_device(
    name = "koi",
    boot_properties = {
        "ro.emulator.circular": "true",
        "ro.emu.win_outset_bottom_px": 20,
        "ro.product.model": "CASIO WSD-F10",
        "ro.product.name": "koi",
    },
    cache = 32,
    # TODO(b/30588599): Remove this once uma emulators can be built.
    emulator_types = [
        "qemu",
        "qemu2",
    ],
    horizontal_resolution = 320,
    min_api = 23,
    ram = 512,
    screen_density = 240,
    system_image_flavors = ["wear"],
    vertical_resolution = 300,
    vm_heap = 48,
)
