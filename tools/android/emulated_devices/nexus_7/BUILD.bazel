# Description:
#   Nexus 7 runs JellyBean
#   Specs from: http://www.google.com/nexus/7/specs/
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "nexus_7",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "asus",
        "ro.product.model": "Nexus 7",
        "ro.build.id": "JRN84D",
    },
    cache = 32,
    horizontal_resolution = 800,
    min_api = 16,
    ram = 1024,
    screen_density = 213,  # hardware spec says 216, but reported in ro.sf.lcd_density as 213.
    vertical_resolution = 1280,
    vm_heap = 256,
)
