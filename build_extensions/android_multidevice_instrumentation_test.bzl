"""Utility for running single test on multiple emulator targets."""

def android_multidevice_instrumentation_test(name, target_devices, **kwargs):
  """Generates a android_instrumentation_test rule for each given device.

  Args:
    name: Name prefix to use for the rules. The name of the generated rules will follow:
      name + target_device[-6:] eg name_15_x86
    target_devices: array of device targets
    **kwargs: arguments to pass to generated android_test rules
  """
  for device in target_devices:
    native.android_instrumentation_test(
        name = name + "_" + device[-6:],
        target_device = device,
        **kwargs
    )

DEVICES = [
    #"//tools/android/emulated_devices/generic_phone:android_15_x86",
    #"//tools/android/emulated_devices/generic_phone:android_16_x86",
    #"//tools/android/emulated_devices/generic_phone:android_17_x86",
    #"//tools/android/emulated_devices/generic_phone:android_18_x86",
    "//tools/android/emulated_devices/generic_phone:android_19_x86_qemu2"
    #"//tools/android/emulated_devices/generic_phone:android_21_x86",
    #"//tools/android/emulated_devices/generic_phone:android_22_x86",
    #"//tools/android/emulated_devices/generic_phone:android_23_x86",
    #"//tools/android/emulated_devices/generic_phone:android_24_x86",
    #"//tools/android/emulated_devices/generic_phone:android_25_x86",
    # use google image for 26 since android image is not final 26
    #"//tools/android/emulated_devices/generic_phone:google_26_x86",
    #"//tools/android/emulated_devices/generic_phone:android_27_x86",
    #"//tools/android/emulated_devices/generic_phone:google_28_x86",
]
