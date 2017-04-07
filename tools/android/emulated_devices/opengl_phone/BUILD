# Description:
#   Generic phone with OpenGL support.
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "opengl_phone",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    cache = 32,
    horizontal_resolution = 480,
    min_api = 16,
    ram = 2048,
    screen_density = 240,
    vertical_resolution = 800,
    vm_heap = 256,
)
