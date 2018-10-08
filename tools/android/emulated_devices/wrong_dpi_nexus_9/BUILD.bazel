# Description:
#   Nexus 9 runs from Lollipop
#   Specs from: http://www.google.com/nexus/9/
#
#   target name format:
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# Real display resolution is 2048x1536, 280 dpi,
# but at the moment emulator does not support this high display resolution.
# See: http://b/7660023

make_device(
    name = "nexus_9",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "htc",
        "ro.product.model": "Nexus 9",
        "ro.build.id": "LRX09D",
    },
    cache = 32,
    horizontal_resolution = 1024,  # 2048
    min_api = 21,
    ram = 1835,
    screen_density = 140,  # 280
    vertical_resolution = 768,  # 1536
    vm_heap = 256,
)
