# Description:
#   A generic Android Go device.

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

# The Go image is not ready for prime time; see b/36686000 for updates. In the
# mean time, you can test against low_memory_phone if you want to ensure that
# your app works on a low memory device.
package(default_visibility = ["//visibility:private"])

make_device(
    name = "generic_go_phone",
    boot_properties = {
        "ro.config.low_ram": "true",
    },
    cache = 32,
    horizontal_resolution = 480,
    min_api = 27,
    ram = 512,
    screen_density = 240,
    system_image_flavors = ["go"],
    vertical_resolution = 800,
    vm_heap = 48,
)
