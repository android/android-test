"""Defines common device targets for unit tests."""

# TODO: consider expanding set of devices. for now just use API 19 only
# since it seems most stable
_ALL_DEVICES = [
    struct(sdk = 19, target = "//tools/android/emulated_devices/generic_phone:android_19_x86"),
]

def devices(min_sdk = 15, max_sdk = 10000):
    """Returns target_devices for android_instrumentation_tests.

    Args:
      min_sdk: the minimum android api level to return. Default 15
      max_sdk: the maximum android api level to return. Default 10000

    Returns:
      list of device targets
    """
    devices = []
    for device in _ALL_DEVICES:
        if (device.sdk >= min_sdk and device.sdk <= max_sdk):
            devices.append(device.target)

    return devices