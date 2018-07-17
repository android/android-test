# Description:
#   Emulated ARCore-ready phone, https://developers.google.com/ar/develop/java/emulator
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "arcore_phone",
    avd_properties = {
        "hw.camera.back": "virtualscene",
        "hw.camera.front": "emulated",
    },
    cache = 32,
    horizontal_resolution = 720,
    min_api = 27,
    ram = 4096,
    screen_density = 267,
    vertical_resolution = 1280,
    vm_heap = 512,
)
