"""Defines the emulators and their supported configs in android_test_support."""

load(
    "//tools/android/emulated_devices:macro/emulator.bzl",
    "new_emulator",
    "emulator_type",
    "emulator_files",
)
load("//tools/android/emulated_devices:macro/image.bzl", "image_files", "image_compressed_suffix")
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
    # We're phasing out qemu1, so only whitelisted projects can depend on
    # explicit qemu1 device targets.
    default_visibility = [
        "//googlex/gcam/hdrplus:__subpackages__",
        "//gws/fruit:__subpackages__",
        "//java/com/google/android/apps/auto:__subpackages__",
        "//java/com/google/android/apps/docs/drive:__subpackages__",
        "//java/com/google/android/apps/auth/test/support:__subpackages__",
        "//java/com/google/android/apps/calendar/calendar:__subpackages__",
        "//java/com/google/android/apps/cardviewer:__subpackages__",
        "//java/com/google/android/apps/gmail:__subpackages__",
        "//java/com/google/android/apps/gsa/binaries/speechservices:__subpackages__",
        "//java/com/google/android/apps/mymaps:__subpackages__",
        "//java/com/google/android/apps/nbu/freighter:__subpackages__",
        # TODO(b/70903098): remove this when issue resolved.
        "//java/com/google/android/apps/play/games:__subpackages__",
        "//java/com/google/android/apps/shopping/express:__subpackages__",
        # TODO(b/68017107): remove this when issue resolved.
        "//java/com/google/android/apps/xcn/longfei:__subpackages__",
        "//java/com/google/android/apps/youtube/app:__subpackages__",
        "//java/com/google/android/clockwork/home:__subpackages__",
        "//java/com/google/android/gmscore:__subpackages__",
        # TODO(b/70864545): remove this when issue resolved.
        "//java/com/google/android/instantapps:wh_group",
        "//java/com/google/gws/tools/carddevserver:__subpackages__",
        "//java/com/google/testing/screendiffing/android/simpleactivity:__subpackages__",
        "//javatests/com/google/android/apps/auto/launcher:__subpackages__",
        "//javatests/com/google/android/apps/calendar/uitest/groomcardscuba:__subpackages__",
        "//javatests/com/google/android/apps/calendar/uitest/timelinechipscuba:__subpackages__",
        "//javatests/com/google/android/apps/common/testing/services/location:__subpackages__",
        "//javatests/com/google/android/apps/contacts/integration/uidatatests/mocktests:__subpackages__",
        "//javatests/com/google/android/apps/gmail:__subpackages__",
        "//javatests/com/google/android/apps/gsa:__subpackages__",
        "//javatests/com/google/android/apps/mymaps:__subpackages__",
        "//javatests/com/google/android/apps/nbu/freighter/integration:__subpackages__",
        "//javatests/com/google/android/apps/playconsole/instrumentation:__subpackages__",
        # TODO(b/70903098): remove this when issue resolved.
        "//javatests/com/google/android/apps/play/games:__subpackages__",
        "//javatests/com/google/android/apps/shopping/express:__subpackages__",
        # TODO(b/68017107): remove this when issue resolved.
        "//javatests/com/google/android/apps/xcn/longfei/app/ui:__subpackages__",
        "//javatests/com/google/android/apps/youtube/app/functional/fakes/social:__subpackages__",
        "//javatests/com/google/android/clockwork/home/espresso/quicksettings:__subpackages__",
        "//javatests/com/google/android/gmscore:__subpackages__",
        "//javatests/com/google/android/libraries/analytics/testing/seaworld:__subpackages__",
        "//javatests/com/google/testing/screendiffing/android/simpleactivity:__subpackages__",
        "//third_party/java_src/android_libs/aosp_calendar/tests/espresso:__subpackages__",
        "//third_party/java/android_apps/exchange/testing/utilities:__subpackages__",
        "//tools/android/python:__subpackages__",
    ]
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
]

# QEMU2 is the new hotness. It requires a different kernel to work. We're
# backporting support to older api levels, but it is slow going.
QEMU2 = new_emulator(
    "qemu2",
    extra_files = [
        "@androidsdk//:qemu2_x86",
    ],
    props = new_props(boot_properties = {_EMULATOR_TYPE_PROP: "qemu2"}),
    supports = {"x86": QEMU2_APIS},
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
  if emulator_type(emulator) == emulator_type(QEMU2):
    maybe_extra_kernel_target = "%s_qemu2_extra" % image_files(image)
    contents.append(maybe_extra_kernel_target)
  return contents
