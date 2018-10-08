# Description:
#   Pixel XL runs from Nougat 7.1
#   Specs from: https://store.google.com/product/pixel_phone
#
#   target name format:
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

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
    horizontal_resolution = 1440,
    min_api = 25,
    ram = 4096,
    screen_density = 534,
    vertical_resolution = 2560,
    vm_heap = 512,
)
