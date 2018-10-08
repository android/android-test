# Description:
#   Galaxy Nexus runs from ICS to JellyBean
#   Specs from: http://en.wikipedia.org/wiki/Nexus_4
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "nexus_4",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "lge",
        "ro.product.model": "Nexus 4",
        "ro.build.id": "JDQ39",
    },
    cache = 32,
    horizontal_resolution = 768,
    min_api = 17,
    ram = 2048,
    screen_density = 280,
    vertical_resolution = 1280,
    vm_heap = 256,
)
