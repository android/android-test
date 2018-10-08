# Description:
#   Samsung Galaxy S III runs from ICS to JellyBean
#   Specs from: http://en.wikipedia.org/wiki/Samsung_Galaxy_S_III
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "galaxy_s_iii",
    boot_properties = {
        "ro.product.brand": "samsung",
        "ro.product.manufacturer": "samsung",
        "ro.product.model": "SCH-I535",
        "ro.build.id": "IMM76D",
    },
    cache = 32,
    horizontal_resolution = 720,
    max_api = 16,
    min_api = 15,
    ram = 1024,
    screen_density = 316,
    vertical_resolution = 1280,
    vm_heap = 256,
)
