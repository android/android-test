# Description:
#   Galaxy Nexus runs from ICS to JellyBean
#   Specs from: http://en.wikipedia.org/wiki/Galaxy_Nexus
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "galaxy_nexus",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "samsung",
        "ro.product.model": "Galaxy Nexus",
        "ro.build.id": "IMM76D",
    },
    cache = 32,
    horizontal_resolution = 720,
    max_api = 18,
    min_api = 15,
    ram = 1024,
    screen_density = 320,  # hardware spec says 316, but reported in ro.sf.lcd_density as 320.
    vertical_resolution = 1280,
    vm_heap = 256,
)
