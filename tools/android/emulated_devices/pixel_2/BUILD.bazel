# Description:
#   Pixel 2 runs from Oreo 8.0
#   Specs from: https://store.google.com/product/pixel_2
#
#   target name format:
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

make_device(
    name = "pixel_2",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "google",
        "ro.product.model": "Pixel 2",
        "ro.opa.eligible_device": "true",
    },
    cache = 32,
    horizontal_resolution = 1080,
    min_api = 26,
    ram = 4096,
    screen_density = 441,
    vertical_resolution = 1920,
    vm_heap = 512,
)
