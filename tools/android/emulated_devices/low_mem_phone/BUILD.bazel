# Description:
#   Low memory phones to test and analyze emerging market memory requirements.
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "low_mem_phone",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.config.low_ram": "true",
    },
    cache = 32,
    horizontal_resolution = 480,
    min_api = 10,
    ram = 512,
    screen_density = 240,
    vertical_resolution = 800,
    vm_heap = 48,
)
