# Description:
#   Pixel XL runs from Nougat 7.1
#   Specs from: https://store.google.com/product/pixel_phone
#
#   target name format:
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

# Real display resolution is 1440x2560, 534 dpi,
# but at the moment emulator does not support this high display resolution.
# See: http://b/7660023

make_device(
    name = "pixel_xl",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "google",
        "ro.product.model": "Pixel XL",
        "ro.opa.eligible_device": "true",
    },
    cache = 32,
    horizontal_resolution = 720,  # 1440,
    min_api = 25,
    ram = 4096,
    screen_density = 267,  # 534
    vertical_resolution = 1280,  # 2560
    vm_heap = 512,
)
