# Description:
#   Generic headunit runs all available API's for Auto (which is API 26 onwards).
#
#   target name format:
#     :auto_${api_level}_${arch} for vanilla android

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "generic_auto",
    boot_properties = {
        "ro.scuba.model": "Android Automotive",
    },
    cache = 32,
    horizontal_resolution = 800,
    min_api = 26,
    ram = 4096,
    screen_density = 161,
    system_image_flavors = ["auto"],
    vertical_resolution = 480,
    vm_heap = 512,
)
