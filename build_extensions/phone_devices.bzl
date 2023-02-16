"""Defines common device targets for unit tests."""

gmscore_channel = struct(
    NONE = struct(id = 0, suffix = ""),
    PREBUILT = struct(id = 1, suffix = ""),
)

def devices(api_list = None, device_type = "generic_phone", gms_channel = gmscore_channel.NONE, use_slim = False):
    """Returns target_devices for android_instrumentation_tests.

    Currently unsupported in bazel
    """
    return []

def apis(min_api = 15, max_api = 10000, exclude_apis = []):
    """Returns a list of api level ints filtered by input parameters.

    Currently unsupported in bazel

    Args:
      min_api: int: the minimum android api level to return. Default 15
      max_api: int: the maximum android api level to return. Default 10000
      exclude_apis: int list: list of api levels to exclude. Default: empty

    Returns:
      the list of api ints
    """
    return []
