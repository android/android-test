# Description:
#   Nexus 6 runs from Lollipop
#   Specs from: http://www.google.com/nexus/6/
#
#   target name format:
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# Real display resolution is 1440x2560, 560 dpi,
# but at the moment emulator does not support this high display resolution.
# See: http://b/7660023

make_device(
    name = "nexus_6",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "motorola",
        "ro.product.model": "Nexus 6",
        "ro.build.id": "LRX09D",
    },
    cache = 32,
    horizontal_resolution = 720,  # 1440,
    min_api = 21,
    ram = 3072,
    screen_density = 320,  # 560 The dpi should be 280 for half resolution. But old version of emulator map it to 320.
    vertical_resolution = 1280,  # 2560
    vm_heap = 256,
)
