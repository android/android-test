# Description:
#   Emulated Daydream-ready phone runs all available Google API and Daydream SDK, staring OC-MR1.
#
#   target name format:
#     :daydream_${api_level}_${arch} for google daydream add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "daydream_phone",
    boot_properties = {
        "ro.scuba.model": "Daydream Phone",
    },
    cache = 32,
    horizontal_resolution = 720,
    min_api = 26,
    ram = 4096,
    screen_density = 267,
    system_image_flavors = [
        "daydream",
        "google",
    ],
    vertical_resolution = 1280,
    vm_heap = 512,
)
