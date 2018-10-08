# Description:
#   Generic tablet runs all available tablet API's
#
#   target name format:
#     :android_${api_level}_${arch} for vanilla android
#     :google_${api_level}_${arch} for google add-ons

load("//tools/android/emulated_devices:macro/make_device.bzl", "make_device")
load("//tools/android/emulated_devices:macro/emulator_info.bzl", "QEMU2_APIS")

package(default_visibility = ["//visibility:public"])

# ICS throws an exception when mainKeys = no/null
# and shows virtual navigation keys when mainKeys = yes
# (Which is counter intuitive). Break out a separate set of targets for ics.

make_device(
    name = "generic_tablet_ics",
    avd_properties = {
        "hw.mainKeys": "yes",
    },
    boot_properties = {
        "ro.scuba.model": "Generic Tablet ICS",
    },
    cache = 32,
    horizontal_resolution = 1280,
    max_api = 15,
    min_api = 15,
    ram = 1536,
    screen_density = 160,
    vertical_resolution = 800,
    vm_heap = 64,
)

make_device(
    name = "generic_tablet_gb",
    avd_properties = {
        "hw.mainKeys": "yes",
    },
    boot_properties = {
        "ro.scuba.model": "Generic Tablet GB",
    },
    cache = 32,
    horizontal_resolution = 1280,
    max_api = 10,
    min_api = 10,
    ram = 1536,
    screen_density = 160,
    vertical_resolution = 800,
    vm_heap = 64,
)

make_device(
    name = "generic_tablet",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.scuba.model": "Generic Tablet",
    },
    cache = 32,
    horizontal_resolution = 1280,
    max_api = 19,
    min_api = 16,
    ram = 1536,
    screen_density = 160,
    vertical_resolution = 800,
    vm_heap = 128,
)

make_device(
    name = "generic_tablet",
    avd_properties = {
        "hw.mainKeys": "no",
    },
    boot_properties = {
        "ro.scuba.model": "Generic Tablet",
    },
    cache = 32,
    horizontal_resolution = 1280,
    min_api = 21,
    ram = 1536,
    screen_density = 160,
    vertical_resolution = 800,
    vm_heap = 256,
)

INTEG_TEST_VISIBILITY = [
    "//tools/android/emulator:__pkg__",
    "//tools/android/emulator:__pkg__",
]

INTEG_TEST_DEVICES = [(api, arch) for api in QEMU2_APIS for arch in [
    "x86_qemu",
    "x86_qemu2",
]]

INTEG_TARGET_TMPL = "google_%d_%s_props"

INTEG_FILE_TMPL = INTEG_TARGET_TMPL + ".properties"

[genrule(
    name = INTEG_TARGET_TMPL % (api, arch),
    srcs = [":_google_%d_%s_props" % (api, arch)],
    outs = [INTEG_FILE_TMPL % (api, arch)],
    cmd = "cp $< $@",
    visibility = INTEG_TEST_VISIBILITY,
) for api, arch in INTEG_TEST_DEVICES]
