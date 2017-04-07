# Description:
#   Nexus One runs from Froyo to Gingerbread but we no longer build for Froyo
#   Specs from: http://en.wikipedia.org/wiki/Nexus_One
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "nexus_one",
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "htc",
        "ro.product.model": "Nexus One",
        "ro.build.id": "FRF91",
    },
    cache = 32,
    horizontal_resolution = 480,
    max_api = 10,
    min_api = 10,
    ram = 512,
    screen_density = 254,
    vertical_resolution = 800,
    vm_heap = 32,
)
