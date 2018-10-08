# Description:
#   Moto x4 runs from Nougat
#   Specs from: https://en.wikipedia.org/wiki/Moto_X4
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons
load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "moto_x4",
    boot_properties = {
        "ro.product.brand": "motorola",
        "ro.product.manufacturer": "motorola",
        "ro.product.model": "Moto x4",
        "ro.scuba.model": "Moto x4",
    },
    cache = 32,
    horizontal_resolution = 1080,
    min_api = 25,
    ram = 3072,
    screen_density = 424,
    vertical_resolution = 1920,
    vm_heap = 256,
)
