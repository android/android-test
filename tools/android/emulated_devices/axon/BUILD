# Axon is the Android image built by Proxy for deploying onto robots.
# Author: yuchenericwu@google.com (Yuchen Wu)

package(default_visibility = [
    "//googlex/proxy:__subpackages__",
])

android_device(
    name = "axon_qemu2",
    cache = 32,
    default_properties = ":default.properties",
    horizontal_resolution = 480,
    ram = 4096,
    screen_density = 160,
    system_image = "//googlex/proxy/emulation/system_images:axon_emulator_images",
    tags = ["requires-kvm"],
    vertical_resolution = 800,
    vm_heap = 512,
)
