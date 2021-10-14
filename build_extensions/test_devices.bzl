"""Defines common device targets for unit tests."""

# TODO: consider expanding set of devices. for now just use API 19 only
# since it seems most stable
_ALL_DEVICES = [
    struct(sdk = 19, target = "//tools/android/emulated_devices/%s:android_19_x86"),
]

def devices(min_sdk = 15, max_sdk = 10000, exclude_pattern = None, device_type = "generic_phone"):
    """Returns target_devices for android_instrumentation_tests.

    Args:
      min_sdk: the minimum android api level to return. Default 15
      max_sdk: the maximum android api level to return. Default 10000
      exclude_pattern: if specified, skip running on targets whose name include this text
      device_type: The device config to emulate, defined in tools/mobile/devices/android. Default "generic_phone"

    Returns:
      list of device targets
    """
    devices = []
    for device in _ALL_DEVICES:
        if (exclude_pattern and exclude_pattern in device.target):
            continue
        elif (device.sdk < min_sdk or device.sdk > max_sdk):
            continue
        else:
            devices.append(device.target % device_type)

    return devices