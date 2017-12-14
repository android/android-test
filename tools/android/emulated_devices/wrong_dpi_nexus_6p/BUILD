# Description:
#   Nexus 6p runs from Marshmallow
#   Specs from: http://www.google.com/nexus/6p/
#
#   target name format:
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# Real display resolution is 1440x2560, 259 dpi,
# but at the moment emulator does not support this high display resolution.
# See: http://b/7660023

make_device(
    name = "nexus_6p",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "huawei",
        "ro.product.model": "Nexus 6p",
        "ro.build.id": "MDA89D",
    },
    cache = 32,
    horizontal_resolution = 720,  # 1440,
    min_api = 23,
    ram = 3072,
    screen_density = 259,  # 518
    vertical_resolution = 1280,  # 2560
    vm_heap = 256,
)
