"""Generates a set of auto android_device targets."""


def auto_make_device(
        boot_properties,
        system_image_flavors):
    """Generates a set of auto specific android_device targets."""

    new_devices(
        # Make target name simple.
        name = "",
        emulators = [QEMU2],
        hardware = new_hardware(
            cache = 32,
            ram = 4096,
            screen_density = 160,
            horizontal_resolution = 1280,
            vertical_resolution = 800,
            vm_heap = 256,
        ),
        min_api = 28,
         = [
            GEARHEAD_EMBEDDED_PI_CAR_RELEASE,
        ],
        system_image_flavors = system_image_flavors,
        user_props = new_props(
            boot_properties = boot_properties,
        ),
    )

