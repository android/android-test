# Description:
#   Nexus 5x runs from Marshmallow
#   Specs from: http://www.google.com/nexus/5x/
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "HEAP_GROWTH_LIMIT", "make_device")

package(default_visibility = ["//visibility:public"])

# Real display resolution is 1920x1080, 423 dpi,
# but at the moment emulator does not support this high display resolution.
# See: http://b/7660023

make_device(
    name = "nexus_5x",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "lge",
        "ro.product.model": "Nexus 5x",
        "ro.build.id": "MDA89E",
        HEAP_GROWTH_LIMIT: "128m",
    },
    cache = 32,
    horizontal_resolution = 540,  # 1080
    min_api = 23,
    ram = 2048,
    screen_density = 212,  # 423
    vertical_resolution = 960,  # 1920
    vm_heap = 256,
)
