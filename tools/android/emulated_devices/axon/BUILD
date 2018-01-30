# Axon is the Android image built by Proxy for deploying onto robots.
# Author: yuchenericwu@google.com (Yuchen Wu)

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = [
    "//googlex/proxy:__subpackages__",
])

make_device(
    name = "axon",
    cache = 32,
    emulator_types = ["qemu2"],
    horizontal_resolution = 480,
    max_api = 25,
    min_api = 25,
    ram = 4096,
    screen_density = 160,
    system_image_flavors = ["axon"],
    vertical_resolution = 800,
    vm_heap = 512,
)
