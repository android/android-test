# Description:
#   Nexus 10
#   Specs from: http://en.wikipedia.org/wiki/Nexus_10
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# Real display resolution is 1600x2560, 300 dpi,
# but at the moment emulator does not support this high display resolution.
# See: http://b/7660023

make_device(
    name = "nexus_10",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "samsung",
        "ro.product.model": "Nexus 10",
        "ro.build.id": "JDQ39",
        "ro.scuba.model": "Nexus 10",
    },
    cache = 32,
    horizontal_resolution = 1280,  # 2560
    min_api = 16,
    ram = 2048,
    screen_density = 150,  # 300
    vertical_resolution = 800,  # 1600
    vm_heap = 256,
)
