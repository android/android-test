#Description:
#   Android TV device.
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "tv",
    cache = 64,
    horizontal_resolution = 1280,
    max_api = 28,
    min_api = 21,
    ram = 2048,
    screen_density = 96,
    system_image_flavors = ["tv"],
    vertical_resolution = 720,
    vm_heap = 64,
)
