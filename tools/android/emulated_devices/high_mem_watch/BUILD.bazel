#Description:
#   Generic Android Wear device with increased memory so SCUBA tests can
#   allocate large images for screen diffs.
#
#   NOTE: Do NOT use this emulator for any automated tests other than SCUBA;
#         few Wear devices have this much memory and it represents an
#         unrealistic target for QA.

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# TODO: Arm emulators are timing out.

make_device(
    name = "high_mem_watch",
    boot_properties = {
        "ro.emulator.circular": "true",
    },
    cache = 32,
    # TODO(b/30588599): Remove this once uma emulators can be built.
    emulator_types = [
        "qemu",
        "qemu2",
    ],
    horizontal_resolution = 480,
    min_api = 20,
    ram = 2048,
    screen_density = 348,
    system_image_flavors = ["wear"],
    vertical_resolution = 480,
    vm_heap = 256,
)
