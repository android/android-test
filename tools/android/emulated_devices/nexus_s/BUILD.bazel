# Description:
#   Nexus S runs from Gingerbread to JellyBean
#   Specs from: http://en.wikipedia.org/wiki/Nexus_S
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "nexus_s",
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "samsung",
        "ro.product.model": "Nexus S",
        "ro.build.id": "GRI20",
    },
    cache = 32,
    horizontal_resolution = 480,
    max_api = 16,
    min_api = 10,
    ram = 512,
    screen_density = 240,  # hardware spec says 233, but reported in ro.sf.lcd_density as 240.
    vertical_resolution = 800,
    vm_heap = 32,
)

# These targets depend on our prop files. :(
filegroup(
    name = "google_10_x86_props",
    srcs = [":_google_10_x86_props"],
    visibility = [
        "//javatests/com/google/android/apps/authenticator:__pkg__",
        "//javatests/com/google/android/apps/authenticator/api:__pkg__",
    ],
)
