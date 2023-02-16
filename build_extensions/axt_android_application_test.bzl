"""A rule wrapper for generating android_application_test ."""

load("//tools/build_defs/android:rules.bzl", "android_application_test")
load("//tools/mobile/devices/android/build_defs:phone_devices.bzl", "devices")

def axt_android_application_test(device_list = devices(), **kwargs):
    """A wrapper around android_application_test used to support open source

    Discouraged: use axt_android_library_test instead
    """
    android_application_test(
        target_devices = [device.target for device in device_list],
        **kwargs
    )
