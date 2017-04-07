# Description:
#   Galaxy Tab 10.1" runs from Honeycomb to JellyBean
#   Specs from: http://en.wikipedia.org/wiki/Galaxy_Tab_10.1
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "galaxy_tab_10",
    boot_properties = {
        "ro.product.brand": "samsung",
        "ro.product.manufacturer": "samsung",
        "ro.product.model": "GT-P7100",
        "ro.build.id": "HRI83",
    },
    cache = 32,
    horizontal_resolution = 1280,
    max_api = 16,
    min_api = 15,
    ram = 1024,
    screen_density = 160,
    vertical_resolution = 800,
    vm_heap = 288,
)
