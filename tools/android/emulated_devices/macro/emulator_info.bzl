"""Defines the emulators and their supported configs in android_test_support."""

load(
    "//tools/android/emulated_devices:macro/emulator.bzl",
    "emulator_files",
    "emulator_is_qemu2",
    "emulator_type",
    "new_emulator",
)
load("//tools/android/emulated_devices:macro/image.bzl", "image_compressed_suffix", "image_files")
load("//tools/android/emulated_devices:macro/props.bzl", "new_props")

_EMULATOR_TYPE_PROP = "ro.mobile_ninjas.emulator_type"

# QEMU1 is the legacy emulator. It is also currently our default emulator.
# Most of android-emulator's team development work focuses on QEMU2, we're
# actively migrating to it.
QEMU = new_emulator(
    "qemu",
    props = new_props(boot_properties = {_EMULATOR_TYPE_PROP: "qemu"}),
    supports = {
        "x86": [
            10,
            15,
            16,
            17,
            18,
            19,
            20,
            21,
            22,
            23,
            24,
            25,
        ],
        "arm": [
            10,
            15,
            16,
            17,
            18,
            19,
        ],
    },
    qemu2 = False,
)

QEMU2_APIS = [
    10,
    15,
    16,
    17,
    18,
    19,
    21,
    22,
    23,
    24,
    25,
    26,
    27,
    28,
    29,
]

# QEMU2 is the new hotness. It requires a different kernel to work. We're
# backporting support to older api levels, but it is slow going.
QEMU2 = new_emulator(
    "qemu2",
    extra_files = [
        "@androidsdk//:qemu2_x86",
    ],
    props = new_props(boot_properties = {_EMULATOR_TYPE_PROP: "qemu2"}),
    supports = {
        "x86": QEMU2_APIS,
        "arm": [
            21,
            22,
            23,
            24,
        ],
    },
    qemu2 = True,
)

def _t2e():
    t2e = dict()
    for e in [QEMU, QEMU2]:
        t2e[emulator_type(e)] = e
    return t2e

TYPE_TO_EMULATOR = _t2e()

def extra_system_image_contents(emulator, image):
    """Returns a list of targets to include the the system image filegroup.

    Mostly this is figured out by information stored in the emulator and image
    objects.

    For QEMU2 we have to add an extra target to get the ranchu kernel.

    Arguments:
      emulator: an emulator
      image: an image
    Returns:
      a list of srcs to put in the file system image filegroup.
    """
    contents = [image_files(image) + image_compressed_suffix(image)]
    contents += emulator_files(emulator)
    if emulator_is_qemu2(emulator):
        maybe_extra_kernel_target = "%s_qemu2_extra" % image_files(image)
        contents.append(maybe_extra_kernel_target)
    return contents
