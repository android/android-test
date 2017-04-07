# Description:
#   Generic tablet with OpenGL support.
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "opengl_tablet",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    cache = 32,
    horizontal_resolution = 1280,
    max_api = 16,
    min_api = 16,
    ram = 1536,
    screen_density = 160,
    vertical_resolution = 800,
    vm_heap = 64,
)
