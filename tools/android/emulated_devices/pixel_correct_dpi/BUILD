# Description:
#   Pixel runs from Nougat 7.1
#   Specs from: https://store.google.com/product/pixel_phone
#
#   target name format:
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")

package(default_visibility = ["//java/com/google/android/apps/internal/assistant:assistant_app_pkg"])

make_device(
    name = "pixel",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.product.brand": "google",
        "ro.product.manufacturer": "google",
        "ro.product.model": "Pixel",
        "ro.opa.eligible_device": "true",
    },
    cache = 32,
    horizontal_resolution = 1080,
    min_api = 25,
    ram = 4096,
    screen_density = 441,
    vertical_resolution = 1920,
    vm_heap = 512,
)
