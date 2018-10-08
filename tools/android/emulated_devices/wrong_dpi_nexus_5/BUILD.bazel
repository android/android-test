# Description:
#   Nexus 5 runs from KitKat
#   Specs from: http://www.google.com/nexus/5/
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "HEAP_GROWTH_LIMIT", "make_device")

package(default_visibility = ["//visibility:public"])

# Real display resolution is 1920x1080, 480 dpi,
# but at the moment emulator does not support this high display resolution.
# See: http://b/7660023

make_device(
    name = "nexus_5",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "lge",
        "ro.product.model": "Nexus 5",
        "ro.build.id": "KRT16M",
        HEAP_GROWTH_LIMIT: "128m",
    },
    cache = 32,
    horizontal_resolution = 540,  # 1080
    min_api = 19,
    ram = 2048,
    screen_density = 240,  # 480
    vertical_resolution = 960,  # 1920
    vm_heap = 256,
)
