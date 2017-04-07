# Description:
#   Pixel C runs Nougat 7.0
#   Specs from: https://store.google.com/product/pixel_c
#
#   target name format:
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# Real display resolution is 2560x1800, 308 dpi,
# but at the moment emulator does not support this high display resolution.
# See: http://b/7660023

make_device(
    name = "pixel_c",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "google",
        "ro.product.model": "Pixel C",
    },
    cache = 32,
    horizontal_resolution = 1280,  # 2560,
    min_api = 24,
    ram = 3072,
    screen_density = 154,  # 308
    vertical_resolution = 900,  # 1800
    vm_heap = 512,
)
