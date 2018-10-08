# Description:
#   Generic phone runs all available API's but we no longer support building for Froyo or Eclair
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")


package(default_visibility = ["//visibility:public"])

make_device(
    name = "generic_phone",
    boot_properties = {
        "ro.scuba.model": "Generic Phone",
    },
    cache = 32,
    horizontal_resolution = 480,
    max_api = 15,
    min_api = 10,
    ram = 1024,
    screen_density = 240,
    vertical_resolution = 800,
    vm_heap = 48,
)

make_device(
    name = "generic_phone",
    boot_properties = {
        "ro.scuba.model": "Generic Phone",
    },
    cache = 32,
    horizontal_resolution = 480,
    max_api = 19,
    min_api = 16,
    ram = 1536,
    screen_density = 240,
    vertical_resolution = 800,
    vm_heap = 128,
)

make_device(
    name = "generic_phone",
    boot_properties = {
        "ro.scuba.model": "Generic Phone",
    },
    cache = 32,
    horizontal_resolution = 480,
    min_api = 20,
    ram = 2048,
    screen_density = 240,
    vertical_resolution = 800,
    vm_heap = 256,
)
