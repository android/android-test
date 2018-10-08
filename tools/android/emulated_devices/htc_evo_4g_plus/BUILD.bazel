# Description:
#   HTC Evo 4G+ runs Gingerbread and ICS
#   Specs from: http://en.wikipedia.org/wiki/HTC_Evo_4G%2B
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "htc_evo_4g_plus",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "htc",
        "ro.product.manufacturer": "htc",
        "ro.product.model": "EVO",
        "ro.build.id": "GRJ90",
    },
    cache = 32,
    horizontal_resolution = 540,
    max_api = 16,
    min_api = 10,
    ram = 1024,
    screen_density = 256,
    vertical_resolution = 960,
    vm_heap = 256,
)
