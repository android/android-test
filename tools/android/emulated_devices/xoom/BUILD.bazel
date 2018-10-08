# Description:
#   Motorola Xoom
#   Specs from: http://en.wikipedia.org/wiki/Motorola_Xoom
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "xoom",
    boot_properties = {
        "ro.product.brand": "motorola",
        "ro.product.manufacturer": "motorola",
        "ro.product.model": "Xoom",
        "ro.build.id": "HWI69",
    },
    cache = 32,
    horizontal_resolution = 1280,
    max_api = 16,
    min_api = 15,
    ram = 1024,
    screen_density = 160,
    vertical_resolution = 800,
    vm_heap = 256,
)
