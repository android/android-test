"""Image object contains details about a particular set of image files."""

load("//tools/android/emulated_devices:macro/props.bzl", "new_props")

# Whitelist of packages which can use compressed images. Compressed images slow
# down builds, and we plan to get rid of entirely in the near future, so this
# list should be kept minimal.
_COMPRESSED_IMAGE_WHITELIST = [
]

# TODO: Get rid of compressed images once the whitelist is empty.
def new_image(
        api_level,
        flavor,
        files,
        arch,
        device_visibility = None,
        version_string = None,
        props = None,
        supports_gms_channels = False,
        compressed = False):
    """Creates an image object."""
    return {
        "api_level": api_level,
        "files": files,
        "arch": arch,
        "flavor": flavor,
        "device_visibility": device_visibility,
        "version_string": version_string or str(api_level),
        "supports_gms_channels": supports_gms_channels,
        "props": props or new_props(),
        "compressed": compressed,
    }

def image_supports_gms_channels(image):
    return image["supports_gms_channels"]

def image_flavor(image):
    return image["flavor"]

def image_api(image):
    return image["api_level"]

def image_files(image):
    return image["files"]

def image_device_visibility(image):
    if image_compressed(image):
        return _COMPRESSED_IMAGE_WHITELIST
    return image["device_visibility"]

def image_version_string(image):
    return image["version_string"]

def image_arch(image):
    return image["arch"]

def image_props(image):
    return image["props"]

def image_compressed(image):
    return image["compressed"]

def image_compressed_suffix(image):
    if image_compressed(image):
        return "_compressed"
    else:
        return ""
