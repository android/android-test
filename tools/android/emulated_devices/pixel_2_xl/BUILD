# Description:
#   Pixel 2 XL runs from Oreo 8.0
#   Specs from: https://store.google.com/product/pixel_2
#
#   target name format:
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

make_device(
    name = "pixel_2_xl",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "google",
        "ro.product.model": "Pixel 2 XL",
        "ro.opa.eligible_device": "true",
    },
    cache = 32,
    horizontal_resolution = 1440,
    min_api = 26,
    ram = 4096,
    screen_density = 538,
    vertical_resolution = 2880,
    vm_heap = 512,
)
