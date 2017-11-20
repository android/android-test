# Description:
#   Jasper
#   Specs from: go/jasper-skus
#

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//visibility:public"])

make_device(
    name = "coral",
    cache = 32,
    horizontal_resolution = 1280,
    min_api = 24,
    ram = 2048,
    screen_density = 188,
    system_image_flavors = ["jasper"],
    vertical_resolution = 800,
    vm_heap = 256,
)
