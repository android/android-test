#Description:
#   Android Wear Sturgeon device.
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# TODO: Arm emulators are timing out.

make_device(
    name = "sturgeon",
    boot_properties = {
        "ro.emulator.circular": "true",
        "ro.product.manufacturer": "Huawei",
        "ro.product.model": "Huawei Watch",
        "ro.product.name": "sturgeon",
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
    system_image_flavors = [
        "wear",
        "wear-le",
    ],
    vertical_resolution = 400,
    vm_heap = 48,
)
