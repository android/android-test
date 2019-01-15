"""Image info contains all well known system image files."""

load("//tools/android/emulated_devices:macro/image.bzl", "image_api", "new_image")
load("//tools/android/emulated_devices:macro/props.bzl", "new_props")

_IMG_TEMPLATE = (
    "@androidsdk//:emulator_images_%s"
)

_GOOGLE = "google"
_ANDROID = "android"
_WEAR = "wear"
_WEAR_TESTKEYS = "wear-testkeys"
_WEAR_LE = "wear-le"
_WEAR_LE_GFW = "wear-le-gfw"
_TV = "tv"
_AUTO = "auto"
_GO = "go"
_DAYDREAM = "daydream"


def _default_images(api_level, flavors, props = None):
    """Helper function to build standard images."""
    images = []
    for a in ["arm", "x86"]:
        for f in flavors:
            for compressed in (True, False):
                target = "%s_%s_%s" % (f, api_level, a)
                images.append(
                    new_image(
                        flavor = f,
                        api_level = api_level,
                        arch = a,
                        files = _IMG_TEMPLATE % target,
                        compressed = compressed,
                        props = props,
                    ),
                )
    return images

_NUM_CORES_PROP = "hw.cpu.ncore"

_ALL_IMAGES = (
    _default_images(10, [_GOOGLE, _ANDROID]) +
    _default_images(
        15,
        [_GOOGLE, _ANDROID],
        props = new_props(
            avd_properties = {
                _NUM_CORES_PROP: "1",  # b/73511483
            },
        ),
    ) +
    _default_images(16, [_GOOGLE, _ANDROID]) +
    _default_images(17, [_GOOGLE, _ANDROID]) +
    _default_images(18, [_GOOGLE, _ANDROID]) +
    _default_images(19, [_GOOGLE, _ANDROID]) +
    _default_images(20, [_WEAR]) +
    _default_images(21, [_GOOGLE, _ANDROID, _WEAR, _TV]) +
    _default_images(22, [_GOOGLE, _ANDROID, _WEAR, _TV]) +
    _default_images(23, [_GOOGLE, _ANDROID, _WEAR, _TV]) +
    _default_images(24, [
        _GOOGLE,
        _ANDROID,
        _WEAR,
        _TV,
    ]) +
    _default_images(25, [
        _GOOGLE,
        _ANDROID,
        _WEAR,
        _WEAR_LE,
        _WEAR_TESTKEYS,
    ]) +
    _default_images(
        26,
        [_GOOGLE, _ANDROID, _WEAR, _WEAR_LE, _AUTO, _DAYDREAM, _TV],
    ) +
    _default_images(27, [
        _GOOGLE,
        _ANDROID,
        _GO,
        _AUTO,
        _DAYDREAM,
        _TV,
    ]) +
    _default_images(28, [_GOOGLE, _ANDROID, _AUTO, _DAYDREAM, _TV, _WEAR, _WEAR_LE, _WEAR_LE_GFW]) +
    []  # Empty list to make stripping the last element with copybara cleaner.
)

def _api_to_images():
    a2i = {}
    for image in _ALL_IMAGES:
        api = image_api(image)
        images = a2i.get(api, [])
        images.append(image)
        a2i[api] = images
    return a2i

API_TO_IMAGES = _api_to_images()
