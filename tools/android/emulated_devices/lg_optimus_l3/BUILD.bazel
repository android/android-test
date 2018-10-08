# Description:
#   LG Optimus L3 runs Gingerbread and Jelly Bean
#   Specs from:
#     http://www.lg.com/in/mobile-phones/lg-E400-optimus-l3/technical-specifications
#     https://en.wikipedia.org/wiki/LG_Optimus_L3
#     http://androidarea51.com/hotlink/kingmt01/DH/Recovery_log
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "lg_optimus_l3",
    boot_properties = {
        "ro.product.brand": "lge",
        "ro.product.manufacturer": "LGE",
        "ro.product.model": "LG-E400",
        "ro.build.id": "IMM76L",
    },
    cache = 32,
    horizontal_resolution = 240,
    max_api = 16,
    min_api = 9,
    ram = 384,
    screen_density = 125,
    vertical_resolution = 320,
    vm_heap = 256,
)
