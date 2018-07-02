"""Knows how to combine property objects from different sources."""

load("//tools/android/emulated_devices:macro/props.bzl", "props_avd", "props_boot")
load("//tools/android/emulated_devices:macro/image.bzl", "image_api", "image_props")
load("//tools/android/emulated_devices:macro/hardware.bzl", "hardware_props")
load("//tools/android/emulated_devices:macro/emulator.bzl", "emulator_props")

_BOOT_PROP_TMPL = "%s=%s\n"
_AVD_CONFIG_TMPL = "avd_config_ini.%s=%s\n"
_SE_LINUX_PROP = "ro.initial_se_linux_mode"
_SDK_VERSION_PROP = "ro.build.version.sdk"
_PRODUCT_MODEL = "ro.product.model"

_DEFAULT_BOOT_RPOP = [
    ("sdcard_size_mb", "512"),
]

def _is_ro(k):
    return k.startswith("ro.")

def _sanitize_value_for_swiftshader(image, k, v):
    if (image_api(image) >= 24 and k == _PRODUCT_MODEL and
        not v.endswith("(Android)")):
        return "%s (Android)" % v
    return v

def _ro_props(props):
    return [(k, v) for k, v in sorted(props_boot(props).items()) if _is_ro(k)]

def _mutable_props(props):
    return [(k, v) for k, v in sorted(props_boot(props).items()) if not _is_ro(k)]

def generate_prop_content(user_props, emulator, hardware, image):
    """Creates a properties file that coalesces properties from various sources."""
    content = "ro.mobile_ninjas.is_emulated=true\n"
    for k, v in _DEFAULT_BOOT_RPOP:
        if k not in props_boot(user_props):
            props_boot(user_props)[k] = v
    if image_api(image) >= 19 and image_api(image) < 24:
        content += _BOOT_PROP_TMPL % (_SE_LINUX_PROP, "disabled")
    elif image_api(image) >= 24:
        content += _BOOT_PROP_TMPL % (_SE_LINUX_PROP, "permissive")
        content += _BOOT_PROP_TMPL % (_SDK_VERSION_PROP, image_api(image))

    for k, v in _ro_props(emulator_props(emulator)):
        content += _BOOT_PROP_TMPL % (k, v)
    for k, v in _ro_props(user_props):
        v = _sanitize_value_for_swiftshader(image, k, v)
        content += _BOOT_PROP_TMPL % (k, v)
    for k, v in _ro_props(hardware_props(hardware)):
        v = _sanitize_value_for_swiftshader(image, k, v)
        content += _BOOT_PROP_TMPL % (k, v)
    for k, v in _ro_props(image_props(image)):
        v = _sanitize_value_for_swiftshader(image, k, v)
        content += _BOOT_PROP_TMPL % (k, v)

    for k, v in _mutable_props(image_props(image)):
        content += _BOOT_PROP_TMPL % (k, v)
    for k, v in _mutable_props(hardware_props(hardware)):
        content += _BOOT_PROP_TMPL % (k, v)
    for k, v in _mutable_props(user_props):
        content += _BOOT_PROP_TMPL % (k, v)
    for k, v in _mutable_props(emulator_props(emulator)):
        content += _BOOT_PROP_TMPL % (k, v)

    for k, v in sorted(props_avd(image_props(image)).items()):
        content += _AVD_CONFIG_TMPL % (k, v)
    for k, v in sorted(props_avd(hardware_props(hardware)).items()):
        content += _AVD_CONFIG_TMPL % (k, v)
    for k, v in sorted(props_avd(user_props).items()):
        content += _AVD_CONFIG_TMPL % (k, v)
    for k, v in sorted(props_avd(emulator_props(emulator)).items()):
        content += _AVD_CONFIG_TMPL % (k, v)
    return content
