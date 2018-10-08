# Description:
#   Droid Razr runs from ICS to JellyBean
#   Specs from: http://en.wikipedia.org/wiki/Droid_Razr
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "droid_razr",
    boot_properties = {
        "ro.product.brand": "motorola",
        "ro.product.manufacturer": "motorola",
        "ro.product.model": "DROID RAZR",
        "ro.build.id": "6.7.2-180_DHD-16_M4-31",
    },
    cache = 32,
    horizontal_resolution = 540,
    max_api = 16,
    min_api = 15,
    ram = 1024,
    screen_density = 256,
    vertical_resolution = 960,
    vm_heap = 256,
)
